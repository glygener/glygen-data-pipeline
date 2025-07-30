package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.CHAIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.CHAIN_SEQUENCE_MAPPING;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.DATABASE;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.METHOD;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PROTEIN_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestAccessionSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class StructureAppenderTest {
    private Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForStructureResource() {
        Model outModel = getDefaultTestOutModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(getDefaultTestAccessionSet());

        StructureAppender appender = new StructureAppender();
        appender.appendData(dataset);

        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein rdfs:seeAlso ?structuredResource . " +
                "       ?structuredResource rdf:type <http://purl.uniprot.org/core/Structure_Resource> . " +
                "       ?structuredResource up:database ?database . " +
                "       ?structuredResource up:method ?method . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertNotNull(solution.getResource(DATABASE));
            assertNotNull(solution.getResource(METHOD));
            count++;
        }
        assertEquals(159, count);
    }

    @Test
    void testAppendDataForChainSequenceMapping() {
        Model outModel = getDefaultTestOutModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(getDefaultTestAccessionSet());

        StructureAppender appender = new StructureAppender();
        appender.appendData(dataset);

        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein rdfs:seeAlso ?structuredResource . " +
                "       ?structuredResource rdf:type <http://purl.uniprot.org/core/Structure_Resource> . " +
                "       ?structuredResource up:chainSequenceMapping ?chainSequenceMapping . " +
                "       ?chainSequenceMapping up:chain ?chain . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertNotNull(solution.getResource(CHAIN_SEQUENCE_MAPPING));
            assertNotNull(solution.getLiteral(CHAIN));
            count++;
        }
        assertEquals(161, count);
    }

}