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
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.RESOURCE;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.VALUE;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestAccessionSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class TranscriptResourceAppenderTest {
    private final Model rdfModel = getTestRdfModel();

    private void executeTest(String queryStr, int cnt) {
        GlygenConfig config = new GlygenConfig();
        config.setEnsemblCds("in/ensembl.cds.sample.fa");
        config.setEnsemblPeptide("in/ensembl.pep.sample.fa");
        config.setTranscriptDb("Ensembl");
        config.setCanonical("in/sample.fasta");
        config.setIsoform("in/sample_additional.fasta");

        Model outModel = getDefaultTestOutModel();

        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(getDefaultTestAccessionSet());

        (new SequenceAppender(config)).appendData(dataset);
        (new TranscriptResourceAppender(config)).appendData(dataset);

        Set<String> resSet = new HashSet<>();
        Set<String> valSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            resSet.add(solution.getResource(RESOURCE).getURI());
            if (solution.contains(VALUE))
                valSet.add(solution.getLiteral(VALUE).getString());
        }
        assertEquals(11, resSet.size());
        assertEquals(cnt, valSet.size());
    }

    @Test
    void testAppendDataForTranscriptResource() {
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?resource rdf:type <http://purl.uniprot.org/core/Transcript_Resource> . " +
                "       ?resource up:translatedTo ?translatedTo ." +
                "       optional { ?resource rdf:value ?value . } } ";

        //sample ensemble cds contains only 9 sequences
        executeTest(queryStr, 8);
    }

    @Test
    void testAppendDataForTranslatedSequence() {
        String queryStr = SPARQL_QUERY_BASE_STR +
                " ?resource rdf:type <https://sparql.glygen.org/ontology/Translated_Sequence> . " +
                " optional { ?resource rdf:value ?value . }}";

        //sample ensembl peptide contains only 6 sequences
        executeTest(queryStr, 1);
    }


    @Test
    void testExceptionOnMissingEnsemblCdsFile() {
        GlygenConfig config = new GlygenConfig();
        config.setEnsemblCds("in/ensembl.cds.sample1.fa");
        config.setEnsemblPeptide("in/ensembl.pep.sample.fa");

        assertThrows(RuntimeException.class, () -> new TranscriptResourceAppender(config));
    }

    @Test
    void testExceptionOnMissingEnsemblPeptideFile() {
        GlygenConfig config = new GlygenConfig();
        config.setEnsemblCds("in/ensembl.cds.sample.fa");
        config.setEnsemblPeptide("in/ensembl.pep.sample1.fa");

        assertThrows(RuntimeException.class, () -> new TranscriptResourceAppender(config));
    }
}