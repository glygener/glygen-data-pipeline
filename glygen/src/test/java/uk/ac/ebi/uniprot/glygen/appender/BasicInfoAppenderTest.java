package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class BasicInfoAppenderTest {
    private Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForBasicInfo() {

        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);

        BasicInfoAppender appender = new BasicInfoAppender();
        appender.appendData(dataset);

        // all proteins are accounted & have all fields listed?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:version ?version . " +
                "       ?protein up:created ?created . " +
                "       ?protein up:modified ?modified . " +
                "       ?protein up:mnemonic ?mnemonic . " +
                "       ?protein up:reviewed ?reviewed . " +
                "       ?protein up:existence ?existence . } " ;

        Set<String> protSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            protSet.add(solution.getResource(PROTEIN).getURI());
            assertNotNull(solution.getLiteral(VERSION));
            assertNotNull(solution.getLiteral(CREATED));
            assertNotNull(solution.getLiteral(MODIFIED));
            assertNotNull(solution.getLiteral(MNEMONIC));
            assertNotNull(solution.getLiteral(REVIEWED));
            assertNotNull(solution.getResource(EXISTENCE));
        }
        assertEquals(5, protSet.size());
    }

    @Test
    void testExceptionOnEmptyRdfModel() {
        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(ModelFactory.createDefaultModel(), outModel);
        BasicInfoAppender appender = new BasicInfoAppender();

        assertThrows(RuntimeException.class, () -> appender.appendData(dataset));
    }
}