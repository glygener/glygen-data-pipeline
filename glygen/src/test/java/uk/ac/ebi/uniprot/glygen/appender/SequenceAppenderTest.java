package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.GLYGEN_CORE_NS;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.SEQUENCE;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestAccessionSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class SequenceAppenderTest {
    private final Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForSequence() {
        GlygenConfig config = new GlygenConfig();
        config.setCanonical("in/sample.fasta");
        config.setIsoform("in/sample_additional.fasta");

        Model outModel = getDefaultTestOutModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(getDefaultTestAccessionSet());

        SequenceAppender appender = new SequenceAppender(config);
        appender.appendData(dataset);

        // all sequences are accounted ?
        String queryStr = "prefix gly: <" + GLYGEN_CORE_NS + "> " +SPARQL_QUERY_BASE_STR +
                "       ?sequence rdf:type <http://purl.uniprot.org/core/Simple_Sequence> . " +
                "       ?sequence up:modified ?modified . " +
                "       ?sequence up:version ?version . " +
                "       ?sequence up:mass ?mass . " +
                "       ?sequence up:md5Checksum ?md5Checksum . " +
                "       ?sequence gly:canonical ?canonical . " +
                "       ?sequence up:reviewed ?reviewed . " +
                "       ?sequence rdf:value ?value . " +
                "       optional { ?sequence up:precursor ?precursor . } " +
                "       optional { ?sequence up:fragment ?fragment . } } ";

        Set<String> seqSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            seqSet.add(solution.getResource(SEQUENCE).getURI());
        }
        assertEquals(15, seqSet.size());
    }


    @Test
    void testExceptionOnMissingCanonicalFastaFile() {
        GlygenConfig config = new GlygenConfig();
        config.setCanonical("in/sample1.fasta");
        config.setIsoform("in/sample_additional.fasta");

        assertThrows(RuntimeException.class, () -> new SequenceAppender(config));
    }

    @Test
    void testExceptionOnMissingIsoformFastaFile() {
        GlygenConfig config = new GlygenConfig();
        config.setCanonical("in/sample.fasta");
        config.setIsoform("in/sample_additional1.fasta");

        assertThrows(RuntimeException.class, () -> new SequenceAppender(config));
    }
}