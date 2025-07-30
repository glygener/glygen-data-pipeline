package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.addLiteralIfExists;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getStructureResourceFromRdf;

public class StructureAppender implements DataAppender {

    @Override
    public void appendData(GlygenDataset dataset) {
        Model outModel = dataset.getOutModel();
        ResultSet resultSet = getStructureResourceFromRdf(dataset.getRdfModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String accession = solution.getResource(PROTEIN).getURI();
            String strResId = solution.getResource(STRUCTURED_RESOURCE).getURI();

            Resource csq = outModel.createResource(solution.getResource(CHAIN_SEQUENCE_MAPPING).getURI());
            csq.addProperty(outModel.createProperty(UP_CHAIN), solution.getLiteral(CHAIN));

            Resource strucRes = outModel.createResource(strResId);
            strucRes.addProperty(RDF.type, outModel.createResource(UP_TYPE_STRUCTURE_RESOURCE));
            strucRes.addProperty(outModel.createProperty(UP_DATABASE), solution.getResource(DATABASE));
            strucRes.addProperty(outModel.createProperty(UP_METHOD), solution.getResource(METHOD));
            addLiteralIfExists(strucRes, solution, outModel.createProperty(UP_RESOLUTION), RESOLUTION);
            strucRes.addProperty(outModel.createProperty(UP_CHAIN_SEQUENCE_MAPPING), csq);

            Resource protein = outModel.createResource(accession);
            protein.addProperty(RDFS.seeAlso, strucRes);
        }
    }
}
