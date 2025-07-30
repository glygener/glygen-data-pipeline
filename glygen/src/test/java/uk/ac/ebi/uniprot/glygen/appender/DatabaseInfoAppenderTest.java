package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.IOException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

public class DatabaseInfoAppenderTest {

    @Test
    void testAppendDataCrossReferenceDbDetails() throws IOException {
        GlygenConfig config = ConfigReader.getGlygenConfigList(CrossReferenceAppenderTest.class
                .getResourceAsStream("/tstGlygenConfig.properties"), "").get(0);

        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(getTestRdfModel(), outModel);
        dataset.addDbUri("http://purl.uniprot.org/database/Reactome");
        dataset.addDbUri("http://purl.uniprot.org/database/EMBL");
        dataset.addDbUri("http://purl.uniprot.org/database/Ensembl");

        DatabaseInfoAppender appender = new DatabaseInfoAppender(config);
        appender.appendData(dataset);

        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?database rdf:type <http://purl.uniprot.org/core/Database> . " +
                "       ?database up:abbreviation ?abbreviation . " +
                "       ?database up:category ?category . " +
                "       ?database up:urlTemplate ?urlTemplate . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int dbCount = 0;
        while (resultSet.hasNext()) {
            resultSet.nextSolution();
            ++dbCount;
        }
        assertEquals(3, dbCount);
    }

    @Test
    void testExceptionOnMissingDatabasesRdfFile() {
        GlygenConfig config = new GlygenConfig();
        config.setDatabases("in/testDatabases1.rdf");

        assertThrows(RuntimeException.class, () -> new DatabaseInfoAppender(config));
    }
}
