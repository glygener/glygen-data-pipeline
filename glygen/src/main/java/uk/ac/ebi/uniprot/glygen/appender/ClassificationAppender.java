package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class ClassificationAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Model keyModel;
    private final Model goModel;
    private Model outModel;

    public ClassificationAppender() {
        keyModel = createModelFromRdfFile(GlygenConfig.getKeywords());
        goModel = createModelFromRdfFile(GlygenConfig.getGeneOntologies());
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        outModel = dataset.getOutModel();
        updateKeywordClassificaton(dataset.getRdfModel());
        updateGoClassification(dataset.getRdfModel(), dataset.getAccessionSet());
    }

    private void updateKeywordClassificaton(Model rdfModel) {
        Set<String> keySet = new HashSet<>();

        ResultSet resultSet = getKeywordClassificationsFromRdf(rdfModel);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String classWithId = solution.getResource(CLASSIFIED_WITH).getURI();
            keySet.add(classWithId);

            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(outModel.createProperty(UP_CLASSIFIED_WITH), outModel.createResource(classWithId));
        }

        for (String id : keySet) {
            updateKeywordInfo(outModel.createResource(id), id);
        }

        logger.debug("KeySet count: {}", keySet.size());
    }

    private void updateKeywordInfo(Resource resource, String classId) {
        String prefLabel = getPrefLabelFromKeywordsRdf(keyModel, classId);
        if (prefLabel == null) {
            logger.error("PrefLabel not found for {}", classId);
            throw new GlyGenException("PrefLabel not found for " + classId);
        } else {
            resource.addProperty(RDF.type, outModel.createResource(UP_TYPE_CONCEPT));
            resource.addProperty(SKOS.prefLabel, prefLabel);

            ResultSet resultSet = getAltLabelsFromKeywordsRdf(keyModel, classId);
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                resource.addProperty(SKOS.altLabel, solution.getLiteral(ALT_LABEL));
            }
        }
    }

    private void updateGoInfo(Resource resource, String resId) {

        String goTerm = getTermLabelFromGeneOntologiesRdf(goModel, resId);
        if (goTerm == null || goTerm.isBlank()) {
            // Not an error: obsolete / secondary GO terms may lack labels
            logger.info("GO label not found for {}", resId);
            return;
        }
    
        resource.addProperty(RDF.type, OWL.Class);
        resource.addProperty(RDFS.label, goTerm);
    
        String namespace =
                getClassificationLabelFromGeneOntologiesRdf(goModel, resId);
    
        if (namespace != null && !namespace.isBlank()) {
            resource.addProperty(
                outModel.createProperty(GLY_GO_CLASSIFICATION),
                namespace
            );
        } else {
            logger.debug("GO namespace missing for {}", resId);
        }
    }
    

    private void updateGoClassification(Model rdfModel, Set<String> accSet) {

        Set<String> goSet = new HashSet<>();
        int count = 0;
        for (String acc : accSet) {
            ResultSet rs = getGoClassificationAttributionFromRdf(rdfModel, acc,
                    acc.substring(acc.lastIndexOf(CHAR_FORWARD_SLASH) +1 ));
            while (rs.hasNext()) {
                count++;
                QuerySolution solution = rs.nextSolution();

                Resource attrib = outModel.createResource(solution.getResource(ATTRIBUTION).getURI());
                attrib.addProperty(outModel.createProperty(UP_EVIDENCE), solution.getResource(EVIDENCE));
                if (solution.contains(SOURCE)) {
                    attrib.addProperty(outModel.createProperty(UP_SOURCE), solution.getResource(SOURCE));
                }

                String classWithId = solution.getResource(CLASSIFIED_WITH).getURI();
                goSet.add(classWithId);

                Resource classification = outModel.createResource(solution.getResource(REIF_ID).getURI());
                classification.addProperty(outModel.createProperty(UP_CLASSIFIED_WITH),
                        outModel.createResource(classWithId));
                classification.addProperty(outModel.createProperty(GLY_ATTRIBUTION), attrib);

                Resource protein = outModel.createResource(acc);
                protein.addProperty(outModel.createProperty(UP_CLASSIFIED_WITH), classification);
            }
        }

        for (String id : goSet) {
            updateGoInfo(outModel.createResource(id), id);
        }

        logger.debug("GoSet & total GO classification attributions: {}, {}", goSet.size(), count );
    }
}