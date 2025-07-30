package uk.ac.ebi.uniprot.glygen.appender;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neo4j.driver.Values.parameters;
import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.AT_PATHWAY;
import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.AT_REACTION;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;

/**
 * Reactome - reaction & pathway annotation appender
 */
public class ReactionAnnotationAppender implements DataAppender, AutoCloseable {
    private final Driver driver;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Model outModel;
    private final GlygenConfig config;

    private static final String QUERY_ID = "queryId";
    private static final String REACT_ID = "reactId";
    private static final String INPUT = "input";

    private static final String REACTION_PARTICIPANTS_QUERY = "MATCH (r:ReactionLikeEvent{stId:$reactId})-[ty]->" +
            "(pe:PhysicalEntity) RETURN DISTINCT type(ty) as Type, pe.stId, pe.displayName order by Type ";
    private static final String REACTION_COMPONENT_QUERY = "MATCH (p:Species{taxId:$queryId})--(e:Event)-->" +
            "(c:Compartment) RETURN e.stId, c.displayName; ";
    private static final String REACTION_SUMMARY_QUERY = "MATCH (p:Species{taxId:$queryId})--(e:Event)-->" +
            "(s:Summation) RETURN e.stId, s.text; ";

    private static final String PARTICIPANT_COMPONENT_QUERY = "MATCH (p:Species{taxId:$queryId})--" +
            "(e:PhysicalEntity)-[:compartment]->(c:Compartment) RETURN e.stId, c.displayName;" ;
    private static final String PARTICIPANT_X_REF_QUERY = "MATCH (p:PhysicalEntity)-" +
            "[:referenceEntity|crossReference*]->(ref) RETURN  distinct p.stId, ref.databaseName, ref.identifier";

    private static final String CITATION_INFO_QUERY = "MATCH (r:ReactionLikeEvent{stId:$reactId})-[t1]->" +
            "(pub:Publication) RETURN DISTINCT r.stId, pub.pubMedIdentifier, pub.displayName," +
            " pub.year, pub.journal, pub.volume, pub.pages ";
    private static final String DISEASE_INFO_QUERY = "MATCH (r:ReactionLikeEvent{stId:$reactId})-[t1]->(dis:Disease)" +
            " RETURN DISTINCT dis.databaseName + '-' + dis.identifier as Identifier, dis.displayName";

    private static final String PATHWAY_INFO_QUERY = "MATCH (a:Species{taxId:$queryId})--(r:ReactionLikeEvent)--" +
            "(p:Pathway)-->(s:Summation) RETURN r.stId, p.stId, p.displayName, s.text;" ;


    public ReactionAnnotationAppender(GlygenConfig config) {
        this.config = config;

        if (config.getReactomeReactions() == null) {
            driver = null;
            logger.warn("{}: Skipping Reaction_Annotation as reactomeReactions is not configured ", config.getName());
        } else {
            checkInputFileExists(config.getReactomeReactions());

            try {
                String[] uriInfo = GlygenConfig.getReactomeNeo4jInfo().split("@");
                driver = GraphDatabase.driver(uriInfo[1],
                        AuthTokens.basic(uriInfo[0].substring(0, uriInfo[0].indexOf('/')),
                                uriInfo[0].substring(uriInfo[0].indexOf('/') + 1)));
            } catch (Exception e) {
                logger.error("Exception connecting to reactome neo4j uri info: {}", GlygenConfig.getReactomeNeo4jInfo(),
                        e);
                throw new GlyGenException(
                        "Exception connecting to reactome neo4j: " + GlygenConfig.getReactomeNeo4jInfo());
            }
        }
    }


    @Override
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }


    @Override
    public void appendData(GlygenDataset dataset) {
        if (config.getReactomeReactions() == null) {
            return;
        }

        outModel = dataset.getOutModel();
        dataset.addDbUri(REACTOME_DB);

        addReactions();

        int reactSummaryCount = appendResourceDetail(REACTION_SUMMARY_QUERY, GLY_RXN_SUMMARY);
        int reactCompCount = appendResourceDetail(REACTION_COMPONENT_QUERY, GLY_CELLULAR_LOCATION);
        int partCompCount = appendResourceDetail(PARTICIPANT_COMPONENT_QUERY, GLY_CELLULAR_LOCATION);
        int xrefCount = addParticipantXRefs();

        logger.debug("Reaction summary & component/cell location counts: {}, {}", reactSummaryCount, reactCompCount);
        logger.debug("Participant component/cell location & xref counts: {}, {}", partCompCount, xrefCount);

        addPathways();
    }

    private String getQueryTaxId() {
        return config.getSpeciesTaxId() == null ? config.getTaxId() : config.getSpeciesTaxId();
    }

    private void addPathways() {
        Resource database = outModel.createResource(REACTOME_DB);

        try ( Session session = driver.session() ) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                int pathCount = 0;
                int pathMapCount = 0;
                Result result = tx.run(PATHWAY_INFO_QUERY, parameters(QUERY_ID, getQueryTaxId()));
                while (result.hasNext()) {
                    Record rec = result.next();
                    Resource reaction = outModel.createResource(REACTOME_PREFIX + getValue(rec.get(0)));
                    // continue only if reaction is in outModel
                    if (reaction.hasProperty(RDF.type)) {
                        pathMapCount++;
                        Resource pathway = outModel.createResource(REACTOME_PB_PREFIX + getValue(rec.get(1)));

                        if (!pathway.hasProperty(RDF.type)) {
                            pathCount++;
                            pathway.addProperty(RDF.type, outModel.createResource(AT_PATHWAY));
                            pathway.addProperty(outModel.createProperty(GLY_PATHWAY_DATABASE), database);
                            pathway.addProperty(outModel.createProperty(GLY_PATHWAY_NAME), getValue(rec.get(2)));
                            pathway.addProperty(outModel.createProperty(GLY_PATHWAY_SUMMARY), getValue(rec.get(3)));
                        }
                        reaction.addProperty(outModel.createProperty(GLY_PATHWAY), pathway);
                    }
                }
                logger.debug("Pathway & pathway mapping counts: {}, {}", pathCount, pathMapCount);
                return null;
            });
        }
    }

    private void addReactions() {
        int reactMapCount = 0;
        int reactCount = 0;
        int skipCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(config.getReactomeReactions()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] sr = line.split("\\t");
                if (!sr[0].contains(DASH)) {
                    Resource protein = outModel.createResource(PROTEIN_PREFIX + sr[0]);
                    if (!protein.hasProperty(RDF.type)) {
                        skipCount++;
                        continue;
                    }
                    reactMapCount++;
                    //Resource reaction = outModel.createResource(sr[1]);
                    Resource reaction = outModel.createResource(REACTOME_PREFIX + sr[1]);
                    if (!reaction.hasProperty(RDF.type)) {
                        reactCount++;

                        reaction.addProperty(RDF.type, outModel.createResource(AT_REACTION));
                        reaction.addProperty(outModel.createProperty(GLY_RXN_NAME),
                                outModel.createTypedLiteral(sr[3]));
                        reaction.addProperty(outModel.createProperty(GLY_RXN_EVIDENCE_CODE),
                                outModel.createTypedLiteral(sr[4]));

                        String reactId = sr[1].substring(sr[1].lastIndexOf(CHAR_FORWARD_SLASH) + 1);
                        appendReactionParticipantInfo(reactId, reaction);
                        appendDiseaseInfo(reactId, reaction);
                        appendCitationInfo(reactId, reaction);
                    }
                    protein.addProperty(outModel.createProperty(UP_ANNOTATION), reaction);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading reactome reaction info file: {}", config.getReactomeReactions(), e);
            throw new GlyGenException("Error reading reactome reaction info file: " + config.getReactomeReactions());
        }
        logger.debug("Reaction, reaction mapping & reaction mapping skip counts: {}, {}, {}", reactCount,
                reactMapCount, skipCount);
    }

    private void appendDiseaseInfo(final String reactId, final Resource reaction) {

        try ( Session session = driver.session() ) {
            session.readTransaction((TransactionWork<Void>) tx -> {
                Result result = tx.run(DISEASE_INFO_QUERY, parameters(REACT_ID, reactId));
                while (result.hasNext()) {
                    Record rec = result.next();
                    Resource disease = outModel.createResource(getValue(rec.get(0)));
                    if (!disease.hasProperty(RDF.type)) {
                        disease.addProperty(RDF.type, outModel.createResource(GLY_TYPE_DISEASE_ONTOLOGY));
                        disease.addProperty(outModel.createProperty(GLY_DO_NAME), getValue(rec.get(1)));
                    }
                    reaction.addProperty(outModel.createProperty(GLY_RXN_DISEASE), disease);
                }
                return null;
            });
        }
    }

    private void appendCitationInfo(final String reactId, final Resource reactAnn) {

        try ( Session session = driver.session() ) {
            session.readTransaction((TransactionWork<Void>) tx -> {

                Result result = tx.run(CITATION_INFO_QUERY, parameters(REACT_ID, reactId));
                while (result.hasNext()) {
                    Record rec = result.next();

                    String citationId = CITATION_PREFIX + getValue(rec.get(1));
                    Resource journal = outModel.createResource(citationId);
                    if (!journal.hasProperty(RDF.type)) {
                        journal.addProperty(RDF.type, outModel.createResource(UP_TYPE_JOURNAL_CITATION));
                        journal.addProperty(outModel.createProperty(UP_TITLE), getValue(rec.get(2)));

                        Value val3 = rec.get(3);
                        if (!val3.isNull()) {
                            journal.addProperty(outModel.createProperty(UP_DATE),
                                    outModel.createTypedLiteral(val3.asInt(), XSDDatatype.XSDgYear));
                        } else {
                            logger.debug("Citation year missing: {}", journal.getURI());
                        }
                        journal.addProperty(outModel.createProperty(UP_NAME), getValue(rec.get(4)));

                        Value val5 = rec.get(5);
                        if (!val5.isNull()) {
                            journal.addProperty(outModel.createProperty(UP_VOLUME),
                                    outModel.createTypedLiteral(val5.asInt()));
                        } else {
                            logger.debug("Citation volume missing: {}", journal.getURI());
                        }
                        journal.addProperty(outModel.createProperty(UP_PAGES), getValue(rec.get(6)));
                    }

                    reactAnn.addProperty(outModel.createProperty(UP_CITATION), journal);
                }

                return null;
            });
        }
    }

    private void appendReactionParticipantInfo(final String reactId, final Resource reaction) {

        try ( Session session = driver.session() ) {
            session.readTransaction((TransactionWork<String>) tx -> {
                Result result = tx.run(REACTION_PARTICIPANTS_QUERY, parameters(REACT_ID, reactId));
                while (result.hasNext()) {
                    Record rec = result.next();

                    String partId = getValue(rec.get(1));
                    Resource participant = outModel.createResource(REACTOME_PREFIX + partId);
                    if (!participant.hasProperty(RDF.type)) {
                        participant.addProperty(RDF.type, outModel.createResource(GLY_TYPE_REACTION_PARTICIPANT));
                        participant.addProperty(outModel.createProperty(GLY_PARTICIPANT_NAME),
                                outModel.createLiteral(getValue(rec.get(2))));
                    }

                    reaction.addProperty(outModel.createProperty(getValue(rec.get(0)).equals(INPUT) ?
                            GLY_RXN_INPUT : GLY_RXN_OUTPUT), participant);
                }
                return null;
            });
        }
    }

    private int appendResourceDetail(final String queryStr, final String propKey) {
        try ( Session session = driver.session() ) {
            return session.readTransaction( tx -> {
                int count = 0;
                Result result = tx.run(queryStr, parameters( QUERY_ID, getQueryTaxId() ) );
                while (result.hasNext()) {
                    Record rec = result.next();
                    Resource resource = outModel.createResource(REACTOME_PREFIX + getValue(rec.get(0)));
                    if (resource.hasProperty(RDF.type)) {
                        count++;
                        resource.addProperty(outModel.createProperty(propKey), getValue(rec.get(1)));
                    }
                }
                return count;
            });
        }
    }


    private int addParticipantXRefs() {
        try ( Session session = driver.session() ) {
            return session.readTransaction( tx -> {
                Result result = tx.run(PARTICIPANT_X_REF_QUERY);
                int count = 0;
                while (result.hasNext()) {
                    Record rec = result.next();
                    String partId = getValue(rec.get(0));
                    Resource participant = outModel.createResource(REACTOME_PREFIX + partId);
                    if (participant.hasProperty(RDF.type)) {
                        count++;
                        Resource xref = outModel.createResource(partId + "_xref_" + count++);
                        xref.addProperty(RDF.type, outModel.createResource(GLY_TYPE_X_REF_IDENTIFIER));
                        xref.addProperty(outModel.createProperty(GLY_XREF_ID_TYPE), getValue(rec.get(1)));
                        xref.addProperty(outModel.createProperty(GLY_XREF_ID), getValue(rec.get(2)));

                        participant.addProperty(outModel.createProperty(GLY_XREF_IDENTIFIER), xref);
                    }
                }
                return count;
            });
        }
    }


    private void checkInputFileExists(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            logger.error("File does not exist: {}", fileName);
            throw new GlyGenException("File does not exist: " + fileName);
        } else if (file.isDirectory()) {
            logger.error("File name is a directory: {}", fileName);
            throw new IllegalArgumentException("File name is a directory: " + fileName);
        } else if (!file.canRead()) {
            logger.error("File cannot be read: {}", fileName);
            throw new GlyGenException("File cannot be read: " + fileName);
        }
    }

    private String getValue(Value val) {
        return val.toString().replace("\"", "");
    }
}