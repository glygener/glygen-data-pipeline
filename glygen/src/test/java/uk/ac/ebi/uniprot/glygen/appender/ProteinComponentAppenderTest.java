package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.COMPONENT;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PROTEIN_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

public class ProteinComponentAppenderTest {
    private Model rdfModel = getTestRdfModel();

    private Model getTestModel() {
        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);

        BasicInfoAppender appender = new BasicInfoAppender();
        appender.appendData(dataset);

        new ProteinComponentAppender().appendData(dataset);
        new NameAppender().appendData(dataset);
        return outModel;
    }

    @Test
    void testAppendDataForComponents() {
        Model outModel = getTestModel();

        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:component ?component . " +
                "       ?component rdf:type <http://purl.uniprot.org/core/Part> . " +
                "       ?component up:recommendedName ?recommendedName . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(COMPONENT));
            ++count;
        }
        assertEquals(14, count);
    }
}
