package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.*;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class AnnotationAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GlygenDataset dataset;
    private Model rdfModel;
    private Model outModel;
    private final Model disModel;
    private final Model locModel;
    private final Model rheaModel;
    private final Set<String> diseaseSet = new HashSet<>();

    private static final String CPR_SUFFIX = "_CPR";
    private Resource RHEA_DB;

    public AnnotationAppender(GlygenConfig config) {
        disModel = createModelFromRdfFile(GlygenConfig.getDiseases());
        locModel = createModelFromRdfFile(GlygenConfig.getLocations());
        rheaModel = createModelFromRdfFile(config.getRhea());
    }

    @Override
    public void appendData(GlygenDataset dataSet) {
        dataset = dataSet;
        rdfModel = dataset.getRdfModel();
        outModel = dataset.getOutModel();
        RHEA_DB = outModel.createResource("http://purl.uniprot.org/database/Rhea");

        for (String annType : ANNOTATION_TYPES) {
            int count = 0;
            ResultSet resultSet = getAnnotationFromRdf(rdfModel, annType);
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
                if (!annotation.hasProperty(RDF.type)) {
                    count++;
                }
                updateProperties(annotation, solution, annType);

                Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
                protein.addProperty(outModel.createProperty(UP_ANNOTATION), annotation);
            }
            logger.debug("{}: {} ", annType, count);
        }
        updateLigandInfo(true, LIGAND, UP_LIGAND);
        updateLigandInfo(false, LIGAND_PART, UP_LIGAND_PART);
        updatePositionInfo();
        updateDiseaseInfo();
        updateCellularComponentInfo();
        updateCatalyticAnnotationInfo();
        updateAttributions();
        updateCommentAttribution();
    }

    private void updateProperties(Resource annotation, QuerySolution solution, String annType) {
        annotation.addProperty(RDF.type, outModel.createResource(annType));
        switch (annType) {
            case AT_ACTIVE_SITE:
            case AT_BINDING_SITE: // ligand & ligandPart added by updateLigandInfo(), range by updatePositionInfo()
            case AT_CHAIN:
            case AT_DISULFIDE_BOND:
            case AT_DOMAIN_EXTENT:
            case AT_INTRA_MEMBRANE:
            case AT_MOTIF:
            case AT_NUCLEOTIDE_BINDING:
            case AT_PEPTIDE:
            case AT_PROPEPTIDE:
            case AT_SIGNAL_PEPTIDE:
            case AT_SITE:
            case AT_DISRUPTION_PHENOTYPE:
            case AT_DOMAIN:
            case AT_ACTIVITY_REGULATION:
            case AT_FUNCTION:
            case AT_POLYMORPHISM:
            case AT_PTM:
            case AT_SUBUNIT:
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_CROSS_LINK:
            case AT_GLYCOSYLATION:
            case AT_INITIATOR_METHIONINE:
            case AT_LIPIDATION:
            case AT_MODIFIED_RESIDUE:
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                return;

            case AT_CO_FACTOR:
                addResourceIfExists(outModel, annotation, solution, UP_CO_FACTOR, CO_FACTOR);
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_MASS_SPECTROMETRY:
                addLiteralIfExists(annotation, solution, outModel.createProperty(UP_MEASURED_ERROR), MEASURED_ERROR);
                addLiteralIfExists(annotation, solution, outModel.createProperty(UP_MEASURED_VALUE), MEASURED_VALUE);
                addResourceIfExists(outModel, annotation, solution, UP_METHOD, METHOD);
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_ALTERNATIVE_SEQUENCE:
            case AT_MUTAGENESIS:
                addLiteralIfExists(annotation, solution, outModel.createProperty(UP_SUBSTITUTION), SUBSTITUTION);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_NATURAL_VARIANT:
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, outModel.createProperty(UP_SUBSTITUTION), SUBSTITUTION);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                addResourceIfExists(annotation, solution, RDFS.seeAlso, SEE_ALSO);
                addResourceIfExists(annotation, solution, SKOS.related, RELATED);
                return;

            case AT_DISEASE:
                if (solution.contains(DISEASE)) {
                    annotation.addProperty(outModel.createProperty(UP_DISEASE), solution.getResource(DISEASE));
                    diseaseSet.add(solution.getResource(DISEASE).getURI());
                }
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_SEQUENCE_CAUTION:
                addResourceIfExists(outModel, annotation, solution, UP_CONFLICTING_SEQUENCE, CONFLICTING_SEQUENCE);
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;

            case AT_SEQUENCE_CONFLICT:
                addResourceIfExists(outModel, annotation, solution, UP_CONFLICTING_SEQUENCE, CONFLICTING_SEQUENCE);
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, outModel.createProperty(UP_SUBSTITUTION), SUBSTITUTION);
                return;

            case AT_SUBCELLULAR_LOCATION:
                addResourceIfExists(outModel, annotation, solution, UP_SEQUENCE, SEQUENCE);
                addLiteralIfExists(annotation, solution, RDFS.comment, COMMENT);
                return;
                // ligand & ligandPart added by and range by updatePositionInfo()
            case AT_CATALYTIC_ACTIVITY:
                // catalyticActivity & catalyzedPhysiologicalReaction, added by updateCatalyticAnnotationInfo()
            case AT_BETA_STRAND:
            case AT_HELIX:
            case AT_TURN:
                // only has range. Added by updatePositionInfo()
                return;
            default:
                logger.error("Unexpected Annotation type {} ", solution.getResource(TYPE).getURI());

        }
    }

    private void updateLigandInfo(boolean lFlag, String resName, String propName) {
        ResultSet resultSet = getLigandInfoFromRdf(rdfModel, lFlag);

        int ligCount = 0;
        int annCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            Resource ligResource = outModel.createResource(solution.getResource(resName).getURI());
            if (!ligResource.hasProperty(RDFS.subClassOf)) {
                ligCount++;
                ligResource.addProperty(RDFS.subClassOf, outModel.createResource(solution.getResource(SUB_CLASS_OF)));
                if (solution.contains(LABEL)) ligResource.addProperty(RDFS.label, solution.getLiteral(LABEL));
                if (solution.contains(COMMENT)) ligResource.addProperty(RDFS.comment, solution.getLiteral(COMMENT));
            }
            annCount++;
            Resource bind_annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
            bind_annotation.addProperty(outModel.createProperty(propName), ligResource);
        }

        logger.debug("{} - {}, {} count: {},  ", AT_BINDING_SITE, annCount, resName, ligCount);
    }

    private void updateAttributions() {
        for (String annType : ANNOTATION_TYPES) {
            ResultSet resultSet = getAnnotationAttributionFromRdf(rdfModel, annType);
            int count = handleResultSet(resultSet);
            logger.debug("{} attribution: {} ", annType, count);
        }
    }

    private void updatePositionInfo() {
        HashMap<String, Resource> uriMap = new HashMap<>();
        int count=0;
        for (String annType : ANNOTATION_TYPES) {
            ResultSet resultSet = getPositionInfoFromRdf(rdfModel, annType);
            while (resultSet.hasNext()) {
                count++;
                QuerySolution solution = resultSet.nextSolution();
                String uri = solution.getResource(RANGE).getURI();

                Resource region = uriMap.computeIfAbsent(uri, k -> {
                    Resource rangeRes = outModel.createResource(getUri(RANGE_PREFIX));
                    rangeRes.addProperty(RDF.type, outModel.createResource(FALDO_TYPE_REGION));
                    rangeRes.addProperty(outModel.createProperty(FALDO_BEGIN),
                            dataset.createPosition(solution.getLiteral(BEGIN_POS).getLong()));
                    rangeRes.addProperty(outModel.createProperty(FALDO_END),
                            dataset.createPosition(solution.getLiteral(END_POS).getLong()));
                    return rangeRes;
                } );

                Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
                annotation.addProperty(outModel.createProperty(UP_RANGE), region);
            }
        }
        logger.debug("Total position count: {}", count);
    }

    private void updateDiseaseInfo() {
        int count = 0;
        for (String disId : diseaseSet) {
            ResultSet resultSet = getPrefLabelCommentFromDiseasesRdf(disModel, disId);
            Resource resource = null;
            if (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                resource = outModel.createResource(disId);
                resource.addProperty(RDF.type,outModel.createResource(UP_TYPE_DISEASE));
                resource.addProperty(SKOS.prefLabel, solution.getLiteral(PREF_LABEL));
                resource.addProperty(RDFS.comment, solution.getLiteral(COMMENT));
                resource.addProperty(outModel.createProperty(UP_MNEMONIC), solution.getLiteral(MNEMONIC));
            }
            if (resource != null) {
                count++;
                addResourceProperty(resource, disId, SKOS.altLabel, ALT_LABEL, true, true);
                addResourceProperty(resource, disId, RDFS.seeAlso, SEE_ALSO, false, true);
            }
        }
        logger.debug("Total disease info: {}, missing: {} ", count, diseaseSet.size()-count);
    }

    private void addResourceProperty(Resource resource, String id, Property rdfProp, String property,
            boolean isLiteral, boolean updateDisease) {

        ResultSet resultSet = updateDisease ? getInfoFromDiseasesRdf(disModel, id, rdfProp.toString(), property):
                getInfoFromLocationsRdf(locModel, id, rdfProp.toString(), property);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            if (solution.contains(property)) {
                resource.addProperty(rdfProp,
                        isLiteral ? solution.getLiteral(property) : solution.getResource(property));
            }
        }
    }

    private void updateCellularComponentInfo() {
        int count = 0;
        Set<String> locationSet = new HashSet<>();
        ResultSet resultSet = getCellularLocationsFromRdf(rdfModel);
        while (resultSet.hasNext()) {
            count++;
            QuerySolution solution = resultSet.nextSolution();
            Resource locatedIn = outModel.createResource(solution.getResource(LOCATED_IN).getURI());
            locatedIn.addProperty(outModel.createProperty(UP_CELLULAR_COMPONENT),
                    solution.getResource(CELLULAR_COMPONENT));

            Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
            annotation.addProperty(outModel.createProperty(UP_LOCATED_IN), locatedIn);

            locationSet.add(solution.getResource(CELLULAR_COMPONENT).getURI());
        }
        logger.debug("Total Cellular Component: {}, location: {} ", count, locationSet.size());


        for (String locId : locationSet) {
            resultSet = getPrefLabelCommentFromLocationsRdf(locModel, locId);
            Resource resource = null;
            if (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                resource = outModel.createResource(locId);
                resource.addProperty(RDF.type, outModel.createResource(UP_TYPE_CELLULAR_COMPONENT));
                resource.addProperty(SKOS.prefLabel, solution.getLiteral(PREF_LABEL));
                resource.addProperty(RDFS.comment, solution.getLiteral(COMMENT));
            }
            if (resource != null) {
                addResourceProperty(resource, locId, SKOS.altLabel, ALT_LABEL, true, false);
                addResourceProperty(resource, locId, RDFS.seeAlso, SEE_ALSO, false, false);
            }
        }
    }

    private boolean isValidAttribute(QuerySolution solution, String annResName) {
        String attUri = solution.getResource(ATTRIBUTION).getURI();
        String annUri = solution.getResource(annResName).getURI();
        if (attUri.indexOf(CHAR_HASH) == -1 || annUri.indexOf(CHAR_HASH) == -1) {
            return true;
        }
        return attUri.substring(0, attUri.indexOf(CHAR_HASH)).equals(annUri.substring(0,annUri.indexOf(CHAR_HASH)));
    }

    private void updateCommentAttribution() {
        for (String annType : ANNOTATION_TYPES_WITH_COMMENT_ATTRIB) {
            ResultSet rs = getAnnotationAttributionOfCommentFromRdf(rdfModel, annType);
            int count = handleResultSet(rs);
            logger.debug("Comment attribution {} count: {}", annType, count);
        }
    }


    private int handleResultSet(ResultSet resultSet) {
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            if (isValidAttribute(solution, ANNOTATION)) {
                count++;
                Resource attrib = outModel.createResource(solution.getResource(ATTRIBUTION).getURI());
                attrib.addProperty(outModel.createProperty(UP_EVIDENCE), solution.getResource(EVIDENCE));
                addResourceIfExists(outModel, attrib, solution, UP_SOURCE, SOURCE);

                Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
                annotation.addProperty(outModel.createProperty(GLY_ATTRIBUTION), attrib);
            }
        }
        return count;
    }

    private Resource getReaction(Resource rheaId) {
        Resource resource = outModel.createResource(rheaId.getURI());
        resource.addProperty(RDF.type, outModel.createResource(AT_REACTION));
        Property equation = outModel.createProperty(GLY_EQUATION);
        if (!resource.hasProperty(equation)) {
            resource.addProperty(equation,
                    outModel.createLiteral(getReactionLabelFromRheaRdf(rheaModel,resource.getURI())));
        }
        Property hasEnzyme = outModel.createProperty(GLY_HAS_ENZYME);
        if (!resource.hasProperty(hasEnzyme)) {
            Resource ec = getEnzymeClassificationFromRheaRdf(rheaModel,resource.getURI());
            if (ec != null) {
                resource.addProperty(hasEnzyme, ec);
            }
        }
        resource.addProperty(outModel.createProperty(GLY_REACTION_DATABASE), RHEA_DB);
        return resource;
    }

    private void updateCatalyticAnnotationInfo() {
        ResultSet rs = getCatalyticActivityInfoFromRdf(rdfModel);
        int count = 0;
        while (rs.hasNext()) {
            count++;
            QuerySolution solution = rs.nextSolution();

            Resource catActivity = outModel.createResource(solution.getResource(CATALYTIC_ACTIVITY).getURI());
            catActivity.addProperty(RDF.type, outModel.createResource(UP_TYPE_CATALYTIC_ACTIVITY));
            catActivity.addProperty(outModel.createProperty(UP_CATALYZED_REACTION),
                    getReaction(solution.getResource(CATALYZED_REACTION)));

            addResourceIfExists(outModel, catActivity, solution, UP_ENZYME_CLASS, ENZYME);

            Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
            annotation.addProperty(RDF.type, outModel.createResource(AT_CATALYTIC_ACTIVITY));
            annotation.addProperty(outModel.createProperty(UP_CATALYTIC_ACTIVITY), catActivity);

            if (solution.contains(CATALYZED_PHYSIOLOGICAL_REACTION)) {
                Resource phyActivity =
                        outModel.createResource(solution.getResource(CATALYTIC_ACTIVITY).getURI() + CPR_SUFFIX);
                phyActivity.addProperty(RDF.type, outModel.createResource(UP_TYPE_CATALYTIC_ACTIVITY));
                phyActivity.addProperty(outModel.createProperty(UP_CATALYZED_REACTION),
                        getReaction(solution.getResource(CATALYZED_PHYSIOLOGICAL_REACTION)));

                annotation.addProperty(outModel.createProperty(GLY_CATALYZED_PHYSIOLOGICAL_ACTIVITY), phyActivity);
            }

            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(outModel.createProperty(UP_ANNOTATION), annotation);
        }
        logger.debug("Total Catalytic_Activity_Annotation: {}", count);
        updateCatalyticAnnotationAttributionInfo();
        updateCatalyticActivityEnzymeInfo();
    }

    private void updateCatalyticAnnotationAttributionInfo() {
        ResultSet rs = getCatalyticAnnotationAttributionFromRdf(rdfModel, false);
        int count = updateCatalyticAnnotationAttribution(rs, false);

        rs = getCatalyticAnnotationAttributionFromRdf(rdfModel, true);
        int cpaCount = updateCatalyticAnnotationAttribution(rs, true);
        logger.debug("Total Catalytic_Annotation attributions: {} + {}", count, cpaCount);
    }

    private int updateCatalyticAnnotationAttribution(ResultSet rs, boolean cpaFlag) {
        int count = 0;
        while (rs.hasNext()) {
            count++;
            QuerySolution solution = rs.nextSolution();
            if (isValidAttribute(solution, CATALYTIC_ACTIVITY)) {
                Resource cActivity = outModel.createResource(solution.getResource(CATALYTIC_ACTIVITY).getURI() +
                        (cpaFlag ? CPR_SUFFIX : ""));

                Resource attrib = outModel.createResource(solution.getResource(ATTRIBUTION).getURI());
                attrib.addProperty(outModel.createProperty(UP_EVIDENCE), solution.getResource(EVIDENCE));
                addResourceIfExists(outModel, attrib, solution, UP_SOURCE, SOURCE);

                cActivity.addProperty(outModel.createProperty(GLY_ATTRIBUTION), attrib);
            }
        }
        return count;
    }

    private void updateCatalyticActivityEnzymeInfo() {
        int count = 0;
        ResultSet rs = getCatalyticActivityEnzymesInfoFromRdf(rdfModel);
        while (rs.hasNext()) {
            count++;
            QuerySolution solution = rs.nextSolution();
            Resource catActivity = outModel.createResource(solution.getResource(CATALYTIC_ACTIVITY).getURI());
            catActivity.addProperty(RDF.type, outModel.createResource(UP_TYPE_CATALYTIC_ACTIVITY));
            catActivity.addProperty(SKOS.closeMatch, solution.getResource(CLOSE_MATCH));

            Resource annotation = outModel.createResource(solution.getResource(ANNOTATION).getURI());
            annotation.addProperty(RDF.type, outModel.createResource(AT_CATALYTIC_ACTIVITY));
            annotation.addProperty(outModel.createProperty(UP_CATALYTIC_ACTIVITY), catActivity);
        }
        logger.debug("Catalytic_Activity enzyme count: {}", count);

    }
}
