package uk.ac.ebi.uniprot.glygen.util;

import java.io.InputStream;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;

public class RdfUtility {

    private RdfUtility(){
    }

    public static final String SPARQL_QUERY_PREFIX_STR = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "   prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "   prefix up: <http://purl.uniprot.org/core/> " +
            "   prefix skos: <http://www.w3.org/2004/02/skos/core#> " +
            "   prefix owl: <http://www.w3.org/2002/07/owl#> " +
            "   prefix faldo: <http://biohackathon.org/resource/faldo#> ";

    public static final String SPARQL_QUERY_BASE_STR = SPARQL_QUERY_PREFIX_STR + " select * where {";
    public static final String SPARQL_QUERY_PROTEIN_STR = SPARQL_QUERY_BASE_STR +
            "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . ";

    public static OntModel getOntModelBase(Model base) {
        OntModel model = base == null ? ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM):
                ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, base);
        model.setNsPrefix("up", UNIPROT_CORE_NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("skos", SKOS.getURI());
        model.setNsPrefix("gly", GLYGEN_CORE_NS);
        model.setNsPrefix("owl", OWL.NS);
        model.setNsPrefix("faldo", FALDO_NS);
        return  model;
    }

    public static ResultSet getAllProteinAccessionsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:version ?version . " +
                "       ?protein up:created ?created . " +
                "       ?protein up:modified ?modified . " +
                "       ?protein up:mnemonic ?mnemonic . " +
                "       ?protein up:reviewed ?reviewed . " +
                "       ?protein up:existence ?existence . " +
                "       ?protein up:organism ?organism . }";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getAllStructuredNameFromRdf(Model rdfModel, String nameType) {
        String structName = "?" + nameType;
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?node rdf:type ?type . " +
                "       ?node up:" + nameType + " " + structName + " . OPTIONAL { " +
                structName + " up:fullName ?fullName } OPTIONAL { " +
                structName + " up:shortName ?shortName } OPTIONAL { " +
                structName + " up:ecName ?ecName } " +
                "   filter (?type = <http://purl.uniprot.org/core/Protein> || " +
                "           ?type = <http://purl.uniprot.org/core/Part> )  } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getGeneLabelFromRdf(Model rdfModel, boolean prefLabel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:encodedBy ?encodedBy . " +
                (prefLabel ? "       ?encodedBy skos:prefLabel ?prefLabel . " :
                            "       ?encodedBy up:orfName ?orfName . ") +
                " }";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getGeneAltLabelsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:encodedBy ?encodedBy . " +
                "       ?encodedBy skos:altLabel ?altLabel . }";

        return getResultSet(rdfModel, queryStr);
    }


    public static ResultSet getTissuesFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                "   SELECT DISTINCT ?protein ?isolatedFrom ?prefLabel WHERE { " +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:isolatedFrom ?isolatedFrom . " +
                "       OPTIONAL {?isolatedFrom rdfs:label ?prefLabel } } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static String getPrefLabelFromTissuesRdf(Model tisuModel, String tisuId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?tissue rdf:type <http://purl.uniprot.org/core/Tissue> . " +
                "       ?tissue skos:prefLabel ?prefLabel . " +
                "       filter(?tissue = <" + tisuId + ">) }";

        return getSingleLiteralFromRdfModel(tisuModel, queryStr, PREF_LABEL);
    }

    public static ResultSet getKeywordClassificationsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:classifiedWith ?classifiedWith . " +
                "       filter(regex(str(?classifiedWith), \"keywords\")) } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getMnemonicAndTaxonFromRdf(Model rdfModel, String accession) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:mnemonic ?mnemonic . " +
                "       ?protein up:organism ?organism . " +
                "       filter(?protein = <" + accession + ">) } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static String getPrefLabelFromKeywordsRdf(Model keyModel, String keyId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?concept rdf:type <http://purl.uniprot.org/core/Concept> . " +
                "       ?concept skos:prefLabel ?prefLabel . " +
                "       filter(?concept = <" + keyId + ">) } ";

        return getSingleLiteralFromRdfModel(keyModel, queryStr, PREF_LABEL);
    }

    public static ResultSet getAltLabelsFromKeywordsRdf(Model keyModel, String keyId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?concept rdf:type <http://purl.uniprot.org/core/Concept> . " +
                "       ?concept skos:altLabel ?altLabel . " +
                "       filter(?concept = <" + keyId + ">) } ";

        return getResultSet(keyModel, queryStr);
    }

    public static String getTermLabelFromGeneOntologiesRdf(Model goModel, String goId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       <" + goId + "> rdf:type <http://www.w3.org/2002/07/owl#Class> . " +
                "       <" + goId + "> rdfs:label ?label . }";

        return getSingleLiteralFromRdfModel(goModel, queryStr, LABEL);
    }

    public static String getClassificationLabelFromGeneOntologiesRdf(Model goModel, String goId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       <" + goId + "> rdf:type <http://www.w3.org/2002/07/owl#Class> . " +
                "       <" + goId + "> rdfs:subClassOf* ?superClass .  " +
                "       ?superClass rdf:type <http://www.w3.org/2002/07/owl#Class> . " +
                "       ?superClass rdfs:label ?label . " +
                "       FILTER NOT EXISTS { ?superClass rdfs:subClassOf ?blah } } ";

        return getSingleLiteralFromRdfModel(goModel, queryStr, LABEL);
    }

    public static ResultSet getEnzymeFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?node rdf:type ?type . " +
                "       ?node up:enzyme ?enzyme . " +
                "   filter(?type = <http://purl.uniprot.org/core/Protein> || " +
                "           ?type = <http://purl.uniprot.org/core/Part> )  } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getPropertyListFromEnzymeRdf(Model enzModel, String enzId,
            String rdfProp, String property) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?enzyme rdf:type <http://purl.uniprot.org/core/Enzyme> . " +
                "       ?enzyme <" + rdfProp + "> ?" + property + " . " +
                "   filter(?enzyme = <" + enzId + ">) } ";  // important to filter by enzymeId

        return getResultSet(enzModel, queryStr);
    }

    public static ResultSet getCatalyticActivityFromEnzymeRdf(Model enzModel, String enzId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?enzyme rdf:type <http://purl.uniprot.org/core/Enzyme> . " +
                "       ?enzyme up:activity ?activity . " +
                "       ?activity rdf:type <http://purl.uniprot.org/core/Catalytic_Activity> . " +
                "       ?activity rdfs:label ?label ." +
                "    filter(?enzyme = <" + enzId + ">) } "; // important to filter by enzymeId

        return getResultSet(enzModel, queryStr);
    }

    public static ResultSet getInteractionFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:interaction ?interaction . " +
                "       ?interaction up:xeno ?xeno . " +
                "       ?interaction up:experiments ?experiments . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getAllParticipantInfoFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?participant rdf:type <http://purl.uniprot.org/core/Participant> . " +
                "       ?participant owl:sameAs ?sameAs . " +
                "       OPTIONAL { ?participant rdfs:label ?label } . " +
                "       OPTIONAL { ?sameAs up:mnemonic ?mnemonic } . " +
                "       OPTIONAL { ?sameAs up:organism ?organism } } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getInfoOfParticipantFromRdf(Model rdfModel, String partId) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:interaction ?interaction . " +
                "       ?interaction up:participant <" + partId + "> } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getStructureResourceFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein rdfs:seeAlso ?structuredResource . " +
                "       ?structuredResource rdf:type <http://purl.uniprot.org/core/Structure_Resource> . " +
                "       ?structuredResource up:database ?database . " +
                "       ?structuredResource up:method ?method . " +
                "       ?structuredResource up:chainSequenceMapping ?chainSequenceMapping . " +
                "       ?chainSequenceMapping up:chain ?chain . " +
                "       OPTIONAL {?structuredResource up:resolution ?resolution } } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getAnnotationFromRdf(Model rdfModel, String annotation) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <" + annotation + "> . " +
                "       OPTIONAL {?annotation rdfs:comment ?comment } " +
                "       OPTIONAL {?annotation rdfs:seeAlso ?seeAlso } " +
                "       OPTIONAL {?annotation skos:related ?related } " +
                "       OPTIONAL {?annotation up:cofactor ?cofactor } " +
                "       OPTIONAL {?annotation up:conflictingSequence ?conflictingSequence } " +
                "       OPTIONAL {?annotation up:disease ?disease } " +
                "       OPTIONAL {?annotation up:measuredError ?measuredError } " +
                "       OPTIONAL {?annotation up:measuredValue ?measuredValue } " +
                "       OPTIONAL {?annotation up:method ?method } " +
                "       OPTIONAL {?annotation up:sequence ?sequence } " +
                "       OPTIONAL {?annotation up:substitution ?substitution } " +
                "}";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getCatalyticActivityInfoFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Catalytic_Activity_Annotation> . " +
                "       ?annotation up:catalyticActivity ?catalyticActivity . " +
                "       ?catalyticActivity up:catalyzedReaction ?catalyzedReaction . " +
                "       OPTIONAL {?catalyticActivity up:enzymeClass ?enzyme . } " +
                "       OPTIONAL {?catalyticActivity skos:closeMatch ?closeMatch . } " +
                "       OPTIONAL {?annotation up:catalyzedPhysiologicalReaction ?catalyzedPhysiologicalReaction } }";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getCatalyticActivityEnzymesInfoFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Catalytic_Activity_Annotation> . " +
                "       ?annotation up:catalyticActivity ?catalyticActivity . " +
                "       ?catalyticActivity skos:closeMatch ?closeMatch . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getCellularLocationsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Subcellular_Location_Annotation> . " +
                "       ?annotation up:locatedIn ?locatedIn . " +
                "       ?locatedIn up:cellularComponent ?cellularComponent . }";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getLigandInfoFromRdf(Model rdfModel, boolean flag) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Binding_Site_Annotation> . ";

                if (flag) {
                    queryStr +=  "       ?annotation up:ligand ?ligand . " +
                            "       ?ligand rdfs:subClassOf ?subClassOf . " +
                            "       OPTIONAL { ?ligand rdfs:label ?label . } " +
                            "       OPTIONAL { ?ligand rdfs:comment ?comment . } }";
                } else {
                    queryStr +=  "       ?annotation up:ligandPart ?ligandPart . " +
                            "       ?ligandPart rdfs:subClassOf ?subClassOf . " +
                            "       OPTIONAL { ?ligandPart rdfs:label ?label . } " +
                            "       OPTIONAL { ?ligandPart rdfs:comment ?comment . } }";
                }
        return getResultSet(rdfModel, queryStr);
    }

    // reification query for given annotation
    public static ResultSet getAnnotationAttributionFromRdf(Model rdfModel, String annotation) {
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                    "   SELECT DISTINCT ?annotation ?attribution ?source ?evidence WHERE { " +
                    "       ?reifId rdf:object ?annotation . " +
                    "       ?annotation rdf:type <" + annotation + "> . " +
                    "       ?reifId up:attribution ?attribution . " +
                    "       ?attribution up:evidence ?evidence . " +
                    "       OPTIONAL { ?attribution up:source ?source . } } " ;

        return getResultSet(rdfModel, queryStr);
    }

    // reification query to get attribution of 'comment'
    public static ResultSet getAnnotationAttributionOfCommentFromRdf(Model rdfModel, String annotation) {
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                "   SELECT DISTINCT ?annotation ?attribution ?source ?evidence WHERE { " +
                "       ?annotation rdf:type <" + annotation + "> . " +
                "       ?annotation rdfs:comment ?comment . " +
                "       ?reifId rdf:object ?comment . " +
                "       ?reifId up:attribution ?attribution . " +
                "       ?attribution up:evidence ?evidence . " +
                "       OPTIONAL { ?attribution up:source ?source . } }" ;

        return getResultSet(rdfModel, queryStr);
    }

    // reification query for GO classification
    public static ResultSet getGoClassificationAttributionFromRdf(Model rdfModel, String protId, String accession) {
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                "   SELECT DISTINCT ?reifId ?classifiedWith ?attribution ?source ?evidence WHERE { " +
                "       <" + protId + "> up:classifiedWith ?classifiedWith . " +
                "       ?reifId rdf:object ?classifiedWith . " +
                "       ?reifId up:attribution ?attribution . " +
                "       ?attribution up:evidence ?evidence . " +
                "       OPTIONAL { ?attribution up:source ?source . } " +
                "       filter(regex(str(?classifiedWith), \"GO_\") &&" +
                "                   regex(str(?reifId), \"" + accession + "\") ) }" ;

        return getResultSet(rdfModel, queryStr);
    }

    // reification query for Catalytic_Activity_Annotation
    public static ResultSet getCatalyticAnnotationAttributionFromRdf(Model rdfModel, boolean cpaFlag) {
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                "   SELECT DISTINCT ?catalyticActivity ?attribution ?source ?evidence WHERE { " +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Catalytic_Activity_Annotation> . " +
                "       ?annotation up:catalyticActivity ?catalyticActivity . " +
    (cpaFlag ? "  ?annotation up:catalyzedPhysiologicalReaction ?cpReaction . ?reifId rdf:object ?cpReaction. " :
                "       ?reifId rdf:object ?catalyticActivity . " ) +
                "       ?reifId up:attribution ?attribution . " +
                "       ?attribution up:evidence ?evidence . " +
                "       OPTIONAL { ?attribution up:source ?source . } }" ;

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getPrefLabelCommentFromDiseasesRdf(Model disModel, String disId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?disease rdf:type <http://purl.uniprot.org/core/Disease> . " +
                "       ?disease skos:prefLabel ?prefLabel . " +
                "       ?disease rdfs:comment ?comment . " +
                "       ?disease up:mnemonic ?mnemonic . " +
                "       filter(?disease = <" + disId + ">) }";

        return getResultSet(disModel, queryStr);
    }

    public static String getReactionLabelFromRheaRdf(Model rheaModel, String rheaId) {
        String queryStr = "prefix rh: <http://rdf.rhea-db.org/> " +
                " select ?label where {" +
                "       <" + rheaId + "> rh:equation ?label . } ";
        return getSingleLiteralFromRdfModel(rheaModel, queryStr, LABEL);
    }

    public static Resource getEnzymeClassificationFromRheaRdf(Model rheaModel, String rheaId) {
        String queryStr = "prefix rh: <http://rdf.rhea-db.org/> " +
                " select ?ec where {" +
                "       <" + rheaId + "> rh:ec ?ec . } ";
        return getResourceFromRdfModel(rheaModel, queryStr, EC);
    }

    public static void main(String args[]) {
//        Model rheaModel = createModelFromRdfFile("C:\\Users\\pvasudev\\IdeaProjects\\unp.fw.glygen\\in\\rhea.rdf");
//        ResultSet resultSet = getReactionCitationCountFromRheaRdf(rheaModel);
//        while (resultSet.hasNext()) {
//            QuerySolution solution = resultSet.nextSolution();
//            System.out.println(solution.getResource("node") + " : " + solution.getResource("citation").getURI());
//        }

        Dataset inDataset = TDBFactory.createDataset("C:\\Users\\pvasudev\\IdeaProjects\\unp.fw.glygen\\in\\dbHomoout");
        inDataset.begin(ReadWrite.READ);
        ResultSet resultSet = getReactionAttributionEvidenceFromRdf(inDataset.getDefaultModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            System.out.println(solution.getResource("reaction") + " : " + solution.getResource("source").getURI());
        }
        inDataset.end();
    }

    public static ResultSet getReactionAttributionEvidenceFromRdf(Model model) {
        String queryStr = "   prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "   prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "   prefix up: <http://purl.uniprot.org/core/> select * where { " +
                "   ?node rdf:type <http://purl.uniprot.org/core/Catalytic_Activity> . " +
                "   ?node up:catalyzedReaction ?reaction . " +
                "   ?node <https://sparql.glygen.org/ontology/attribution> ?attribution . " +
                "   ?attribution up:source ?source . } " +
                "order by ?reaction ";

        return getResultSet(model, queryStr);
    }

    public static ResultSet getReactionCitationCountFromRheaRdf(Model rheaModel) {
        String queryStr = "prefix rh: <http://rdf.rhea-db.org/> " +
//                " select ?node (count(*) as ?count) where {" +
                " select * where {" +
                "   ?node rh:citation ?citation . } " +
                "order by ?node ";

        return getResultSet(rheaModel, queryStr);
    }

    public static ResultSet getInfoFromDiseasesRdf(Model disModel, String disId, String rdfProp, String property) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?disease rdf:type <http://purl.uniprot.org/core/Disease> . " +
                "       ?disease <" + rdfProp + "> ?" + property + " . " +
                "       filter (?disease = <" + disId + ">) }";

        return getResultSet(disModel, queryStr);
    }

    public static ResultSet getPrefLabelCommentFromLocationsRdf(Model locModel, String locId) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?location rdf:type <http://purl.uniprot.org/core/Cellular_Component> . " +
                "       ?location skos:prefLabel ?prefLabel . " +
                "       ?location rdfs:comment ?comment . " +
                "       filter (?location = <" + locId + ">) }";

        return getResultSet(locModel, queryStr);
    }

    public static ResultSet getInfoFromLocationsRdf(Model locModel, String locId, String rdfProp, String property) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?location rdf:type <http://purl.uniprot.org/core/Cellular_Component> . " +
                "       ?location <" + rdfProp + "> ?" + property + " . " +
                "       filter (?location = <" + locId + ">) }";

        return getResultSet(locModel, queryStr);
    }

    public static ResultSet getPositionInfoFromRdf(Model rdfModel, String annotation) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type <" + annotation + "> . " +
                "       ?annotation up:range ?range . " +
                "       ?range rdf:type <http://biohackathon.org/resource/faldo#Region> . " +
                "       ?range faldo:begin ?begin . " +
                "       ?range faldo:end ?end . " +
                "       ?begin faldo:position ?beginPos . " +
                "       ?end faldo:position ?endPos . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getProteinComponentsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:component ?component . " +
                "       ?component rdf:type <http://purl.uniprot.org/core/Part> . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getProteinDomainsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:domain ?domain . " +
                "       ?domain rdf:type <http://purl.uniprot.org/core/Part> . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getSequenceInfoFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:reviewed ?reviewed . " +
                "       ?protein up:sequence ?sequence ." +
                "       ?sequence rdf:type <http://purl.uniprot.org/core/Simple_Sequence> . " +
                "       ?sequence up:modified ?modified . " +
                "       ?sequence up:version ?version . " +
                "       ?sequence up:mass ?mass . " +
                "       ?sequence up:md5Checksum ?md5Checksum . " +
                "       ?sequence rdf:value ?value . " +
                "       OPTIONAL { ?sequence up:fragment ?fragment } " +
                "       OPTIONAL { ?sequence up:precursor ?precursor } } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getAllSequenceNamesFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?sequence rdf:type ?type . " +
                "       ?sequence up:name ?name . " +
                "       filter ( ?type = <http://purl.uniprot.org/core/Simple_Sequence> || " +
                "               ?type = <http://purl.uniprot.org/core/Modified_Sequence> )   " +
                "       } ORDER BY ?sequence ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getEnsemblTranscriptsFromRdf(Model rdfModel, String transcriptDb) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?ensTranscript rdf:type <http://purl.uniprot.org/core/Transcript_Resource> . " +
                "       ?ensTranscript up:database <http://purl.uniprot.org/database/" + transcriptDb + "> . " +
                "       ?ensTranscript rdfs:seeAlso ?sequence . " +
                "       ?ensTranscript up:translatedTo ?translatedTo . } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getSingleSeqEnsemblTranscriptsFromRdf(Model rdfModel, String transcriptDb) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:sequence ?sequence ." +
                "       ?protein rdfs:seeAlso ?ensTranscript ." +
                "       ?ensTranscript rdf:type <http://purl.uniprot.org/core/Transcript_Resource> ." +
                "       ?ensTranscript up:database <http://purl.uniprot.org/database/" + transcriptDb + "> . " +
                "       ?ensTranscript up:translatedTo ?translatedTo . " +
                "   FILTER NOT EXISTS { ?ensTranscript rdfs:seeAlso ?seq } } ";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getDbCrossReferenceFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein rdfs:seeAlso ?resource . " +
                "       ?resource rdf:type ?type . " +
                "       ?resource up:database ?database . " +
                "       OPTIONAL { ?resource rdfs:seeAlso ?sequence . } " +
                "       OPTIONAL { ?resource rdfs:comment ?comment . } " +
                "       FILTER ( ?type != <http://purl.uniprot.org/core/Transcript_Resource> && " + // ensTranscript
                "               ?database != <http://purl.uniprot.org/database/PDB> && " + // Structure_Resource
                "               ?database != <http://purl.uniprot.org/database/Reactome>) } ";  // Pathway_Annotation

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getDistinctDbCrossRefDatabasesFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PREFIX_STR + " select distinct ?database where {" +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein rdfs:seeAlso ?resource . " +
                "       ?resource up:database ?database . } ";
        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getDetailsFromDatabasesRdf(Model dbModel) {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?database rdf:type <http://purl.uniprot.org/core/Database> . " +
                "       ?database up:abbreviation ?abbreviation . " +
                "       ?database up:category ?category . " +
                "       ?database up:urlTemplate ?urlTemplate . } ";

        return getResultSet(dbModel, queryStr);
    }

    // getting only journal_citation. Others - Book_Citation, Electronic_Citation, Observation_Citation,
    // Patent_Citation, Submission_Citation & Thesis_Citation filtered for the moment [JeetVora confirmed on 14-Jun-219]
    public static ResultSet getJournalCitationsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:citation ?citation . " +
                "       ?citation rdf:type <http://purl.uniprot.org/core/Journal_Citation> . " +
                "       ?citation up:title ?title . " +
                "       ?citation up:name ?name . " +
                "       ?citation skos:exactMatch ?exactMatch . " +
                "       ?citation up:volume ?volume . " +
                "       ?citation up:pages ?pages . " +
                "       ?citation up:date ?date . " +
                "       OPTIONAL { ?citation <http://purl.org/dc/terms/identifier> ?identifier . } }";

        return getResultSet(rdfModel, queryStr);
    }

    public static ResultSet getCitationAuthorsFromRdf(Model rdfModel, String citId) {
        String queryStr = SPARQL_QUERY_BASE_STR + " <" + citId + "> up:author ?author . }";

        return getResultSet(rdfModel, queryStr);
    }

    public static String getCanonicalSequenceIdFromRdf(Model rdfModel, String accId) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:sequence ?sequence ." +
                "       ?sequence rdf:type <http://purl.uniprot.org/core/Simple_Sequence> ." +
                "       filter (?protein = <http://purl.uniprot.org/uniprot/" + accId + ">) } ";

        // For some accessions like P0DMS8, F8WCM5, other sp entries are isoforms, hence not using
        // getSingleResourceUriFromRdfModel(rdfModel, queryStr, SEQUENCE);
        ResultSet resultSet = getResultSet(rdfModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            if (solution.getResource(SEQUENCE).toString().contains(accId)) {
                String canonical = solution.getResource(SEQUENCE).toString();
                return canonical.substring(canonical.lastIndexOf(CHAR_FORWARD_SLASH) + 1);
            }
        }
        return null;
    }

    public static int getCountFromOutput(Model m, String queryStr) {
        Query query = QueryFactory.create(queryStr);
        try (QueryExecution execution = QueryExecutionFactory.create(query, m)) {
            ResultSet resultSet = execution.execSelect();
            if (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                return solution.getLiteral(COUNT).getInt();
            }
        }

        return -1;
    }

    public static void addLiteralIfExists(Resource resource, QuerySolution solution, Property prop, String propKey) {
        if (solution.contains(propKey)) {
            resource.addProperty(prop, solution.getLiteral(propKey));
        }
    }

    public static void addResourceIfExists(Resource resource, QuerySolution solution, Property prop, String propKey) {
        if (solution.contains(propKey)) {
            resource.addProperty(prop, solution.getResource(propKey));
        }
    }

    public static void addResourceIfExists(Model model, Resource resource, QuerySolution solution,
            String propName, String propKey) {
        if (solution.contains(propKey)) {
            resource.addProperty(model.createProperty(propName), solution.getResource(propKey));
        }
    }

    public static Model createModelFromRdfFile(String fileName) {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(fileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + fileName + " not found");
        }
        model.read(in, null);
        return model;
    }

    public static ResultSet getResultSet(Model rdfModel, String queryStr) {
        Query query = QueryFactory.create(queryStr);
        return QueryExecutionFactory.create(query, rdfModel).execSelect();
    }

    private static String getSingleLiteralFromRdfModel(Model model, String queryStr, String propName) {
        ResultSet resultSet = getResultSet(model, queryStr);
        if (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            return solution.getLiteral(propName).getString();
        }
        return null;
    }

    private static Resource getResourceFromRdfModel(Model model, String queryStr, String propName) {
        ResultSet resultSet = getResultSet(model, queryStr);
        if (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            return solution.getResource(propName);
        }
        return null;
    }

    public static ResultSet getSecondaryAccessionsFromRdf(Model rdfModel) {
        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:replaces ?replaces . }";

        return getResultSet(rdfModel, queryStr);
    }

}