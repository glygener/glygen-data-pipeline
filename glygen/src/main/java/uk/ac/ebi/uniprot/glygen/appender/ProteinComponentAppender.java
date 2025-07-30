package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.COMPONENT;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.DOMAIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UP_COMPONENT;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UP_DOMAIN;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getProteinComponentsFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getProteinDomainsFromRdf;

public class ProteinComponentAppender implements DataAppender {

    @Override
    public void appendData(GlygenDataset dataset) {
        Model outModel = dataset.getOutModel();
        appendData(getProteinComponentsFromRdf(dataset.getRdfModel()), outModel, COMPONENT, UP_COMPONENT);
        appendData(getProteinDomainsFromRdf(dataset.getRdfModel()), outModel, DOMAIN, UP_DOMAIN);
    }

    private void appendData(ResultSet resultSet, Model outModel, String resType, String propType) {
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Resource part = outModel.createResource(solution.getResource(resType).getURI());
            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(outModel.createProperty(propType), part);
        }
    }
}