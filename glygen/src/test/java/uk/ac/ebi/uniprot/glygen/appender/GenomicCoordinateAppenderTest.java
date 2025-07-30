package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestAccessionSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class GenomicCoordinateAppenderTest {
    private static GenomicCoordinateAppender appender = null;
    private static final Model outModel = getDefaultTestOutModel();

    @BeforeAll
    static void setUp() {
        if (appender == null) {
            GlygenConfig config = new GlygenConfig();
            config.setGeneCoordinate("in/test.coordinates.json");
            config.setEnsemblCds("in/ensembl.cds.sample.fa");
            config.setEnsemblPeptide("in/ensembl.pep.sample.fa");
            config.setTranscriptDb("Ensembl");
            config.setCanonical("in/sample.fasta");
            config.setIsoform("in/sample_additional.fasta");

            GlygenDataset dataset = new GlygenDataset(getTestRdfModel(), outModel);
            dataset.createAccessionMap(getDefaultTestAccessionSet());

            (new SequenceAppender(config)).appendData(dataset);
            (new TranscriptResourceAppender(config)).appendData(dataset);

            appender = new GenomicCoordinateAppender(config);
            appender.appendData(dataset);
        }
    }

    @Test
    void testExceptionForInvalidJson() {
        GlygenConfig config = new GlygenConfig();
        config.setGeneCoordinate("in/gene.json");

        assertThrows(RuntimeException.class, () -> new GenomicCoordinateAppender(config));
    }

    @Test
    void testTranscriptResourceAdded() {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?transcript <https://sparql.glygen.org/ontology/reverseStrand> ?reverseStrand . " +
                "       ?transcript <https://sparql.glygen.org/ontology/chromosome> ?chromosome . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        int count = 0;
        while (resultSet.hasNext()) {
            resultSet.nextSolution();
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    void testTranscriptRangeAdded() {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?transcript <https://sparql.glygen.org/ontology/transcriptRange> ?range . " +
                "       ?range rdf:type <http://biohackathon.org/resource/faldo#Region>  . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        int count = 0;
        while (resultSet.hasNext()) {
            resultSet.nextSolution();
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    void testExonsAdded() {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?transcript <https://sparql.glygen.org/ontology/exonRange> ?range . " +
                "       ?range rdf:type <http://biohackathon.org/resource/faldo#Region>  . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        int count = 0;
        while (resultSet.hasNext()) {
            resultSet.nextSolution();
            count++;
        }
        assertEquals(18, count);
    }
}