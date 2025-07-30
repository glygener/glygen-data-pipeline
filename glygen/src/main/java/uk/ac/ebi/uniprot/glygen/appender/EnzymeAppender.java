package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

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

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class EnzymeAppender implements DataAppender {
    private Model outModel;

    public EnzymeAppender() {
        GlygenConfig.createEnzymeModel();
        if (GlygenConfig.getEnzymeModel() == null) {
            throw new GlyGenException("Error creating Enzyme model using " + GlygenConfig.getEnzyme());
        }
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        outModel = dataset.getOutModel();

        Set<String> enzymeSet = new HashSet<>();
        ResultSet resultSet = getEnzymeFromRdf(dataset.getRdfModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            String enzId = solution.getResource(ENZYME).getURI();
            Resource enzyme = outModel.createResource(enzId);
            enzyme.addProperty(RDF.type, outModel.createResource(UP_TYPE_ENZYME));
            enzymeSet.add(enzId);

            Resource node = outModel.createResource(solution.getResource(NODE).getURI());
            node.addProperty(outModel.createProperty(UP_ENZYME), enzyme);
        }
        updateEnzymeInfo(enzymeSet);
    }

    private void updateEnzymeInfo(Set<String> enzymeSet) {
        for (String enzId : enzymeSet) {
            Resource enzyme = outModel.createResource(enzId);
            updateProperty(enzyme, enzId, outModel.createProperty(UP_ACTIVITY), ACTIVITY, false);
            updateProperty(enzyme, enzId, outModel.createProperty(UP_CO_FACTOR_LABEL), CO_FACTOR_LABEL, true);
            updateProperty(enzyme, enzId, outModel.createProperty(UP_OBSOLETE), OBSOLETE, true);
            updateProperty(enzyme, enzId, outModel.createProperty(UP_REPLACED_BY), REPLACED_BY, false);
            updateProperty(enzyme, enzId, outModel.createProperty(UP_REPLACES), REPLACES, false);
            updateProperty(enzyme, enzId, RDFS.subClassOf, SUB_CLASS_OF, false);
            updateProperty(enzyme, enzId, SKOS.prefLabel, PREF_LABEL, true);
            updateProperty(enzyme, enzId, SKOS.altLabel, ALT_LABEL, true);
            updateCatalyticActivityInfo(enzId);
        }
    }

    private void updateProperty(Resource enzyme, String enzId, Property rdfProp, String property, boolean isLiteral) {

        ResultSet resultSet = getPropertyListFromEnzymeRdf(GlygenConfig.getEnzymeModel(),
                enzId, rdfProp.getURI(), property);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            enzyme.addProperty(rdfProp, isLiteral ? solution.getLiteral(property) : solution.getResource(property));
        }
    }

    private void updateCatalyticActivityInfo(String enzId) {
        ResultSet resultSet = getCatalyticActivityFromEnzymeRdf(GlygenConfig.getEnzymeModel(), enzId);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            Resource activity = outModel.createResource(solution.getResource(ACTIVITY).getURI());
            activity.addProperty(RDF.type, outModel.createResource(UP_TYPE_CATALYTIC_ACTIVITY));
            activity.addProperty(RDFS.label, solution.getLiteral(LABEL));
        }
    }
}
