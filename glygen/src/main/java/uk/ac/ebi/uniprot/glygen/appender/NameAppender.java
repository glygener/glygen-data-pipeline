package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.addLiteralIfExists;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getAllStructuredNameFromRdf;

public class NameAppender implements DataAppender {
    private Model rdfModel;
    private Model outModel;

    @Override
    public void appendData(GlygenDataset dataSet) {
        rdfModel = dataSet.getRdfModel();
        outModel = dataSet.getOutModel();

        updateName(RECOMMENDED_NAME, UP_RECOMMENDED_NAME);
        updateName(ALTERNATIVE_NAME, UP_ALTERNATIVE_NAME);
        updateName(SUBMITTED_NAME, UP_SUBMITTED_NAME);
    }

    private void updateName(String nameType, String propNameType) {
        ResultSet resultSet = getAllStructuredNameFromRdf(rdfModel, nameType);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            String nameUri = solution.getResource(nameType).getURI();
            String nodeUri = solution.getResource(NODE).getURI();
            if ((nodeUri.indexOf(CHAR_HASH) == -1 && nameUri.startsWith(nodeUri)) ||
                    (nameUri.indexOf(CHAR_HASH) != -1 && nodeUri.indexOf(CHAR_HASH) != -1 &&
                    nameUri.substring(0, nameUri.indexOf(CHAR_HASH)).equals(nodeUri.substring(0,
                            nodeUri.indexOf(CHAR_HASH))))) {
                Resource structName = outModel.createResource(nameUri);
                structName.addProperty(RDF.type, outModel.createResource(UP_TYPE_STRUCTURED_NAME));
                addLiteralIfExists(structName, solution, outModel.createProperty(UP_FULL_NAME), FULL_NAME);
                addLiteralIfExists(structName, solution, outModel.createProperty(UP_SHORT_NAME), SHORT_NAME);
                addLiteralIfExists(structName, solution, outModel.createProperty(UP_EC_NAME), EC_NAME);

                Resource node = outModel.createResource(nodeUri);
                if (!node.hasProperty(RDF.type)){
                    node.addProperty(RDF.type, solution.getResource(TYPE));
                }
                node.addProperty(outModel.createProperty(propNameType), structName);
            } else if (nameUri.indexOf(CHAR_HASH) == -1) {
                System.out.println("Checkk name " + nameUri);
            }  else if (nodeUri.indexOf(CHAR_HASH) == -1) {
                System.out.println("Checkk node " + nodeUri);
            }
        }
    }

}
