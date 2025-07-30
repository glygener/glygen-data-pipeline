package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class CrossReferenceAppender implements DataAppender {

    @Override
    public void appendData(GlygenDataset dataset) {
        Model outModel = dataset.getOutModel();
        ResultSet resultSet = getDbCrossReferenceFromRdf(dataset.getRdfModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            Resource database = outModel.createResource(solution.getResource(DATABASE).getURI());
            dataset.addDbUri(database.getURI());

            Resource crossRef = outModel.createResource(solution.getResource(RESOURCE).getURI());
            crossRef.addProperty(RDF.type, outModel.createResource(UP_TYPE_RESOURCE));
            addLiteralIfExists(crossRef, solution, RDFS.comment, COMMENT);
            crossRef.addProperty(outModel.createProperty(UP_DATABASE), database);
            addResourceIfExists(crossRef, solution, RDFS.seeAlso, SEQUENCE);

            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(RDFS.seeAlso, crossRef);
        }
    }
}
