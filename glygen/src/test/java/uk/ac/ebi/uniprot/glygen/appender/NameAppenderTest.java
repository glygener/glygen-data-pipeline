package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.EC_NAME;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.FULL_NAME;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.SHORT_NAME;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class NameAppenderTest {
    private Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForRecommendedName() {
        // recommended name for all proteins accounted ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:recommendedName ?recommendedName . " +
                "       optional { ?recommendedName up:fullName ?fullName . } " +
                "       optional { ?recommendedName up:shortName ?shortName . } " +
                "       optional { ?recommendedName up:ecName ?ecName . } } ";

        localTest(queryStr, 4, 4, 1, 1);
    }

    @Test
    void testAppendDataForAlternativeName() {
        // all protein alternative names are accounted ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:alternativeName ?alternativeName . " +
                "       optional { ?alternativeName up:fullName ?fullName . } " +
                "       optional { ?alternativeName up:shortName ?shortName . } " +
                "       optional { ?alternativeName up:ecName ?ecName . } } ";

        localTest(queryStr, 2, 15, 2, 1);
    }


    @Test
    void testAppendDataForSumbittedName() {
        // submitted name all proteins accounted ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:submittedName ?submittedName . " +
                "       optional { ?submittedName up:fullName ?fullName . } " +
                "       optional { ?submittedName up:shortName ?shortName . } " +
                "       optional { ?submittedName up:ecName ?ecName . } } ";

        localTest(queryStr, 1, 1, 0, 0);
    }

    private void localTest(String queryStr, int protCnt, int fullNameCnt, int shortNameCnt, int ecNameCnt) {
        Model outModel = getDefaultTestOutModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);

        NameAppender appender = new NameAppender();
        appender.appendData(dataset);

        Set<String> protSet = new HashSet<>();
        Set<String> fullNameSet = new HashSet<>();
        Set<String> shortNameSet = new HashSet<>();
        Set<String> ecNameSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            protSet.add(solution.getResource(PROTEIN).getURI());
            if (solution.contains(FULL_NAME)) {
                fullNameSet.add(solution.getLiteral(FULL_NAME).getString());
            }
            if (solution.contains(SHORT_NAME)) {
                shortNameSet.add(solution.getLiteral(SHORT_NAME).getString());
            }
            if (solution.contains(EC_NAME)) {
                ecNameSet.add(solution.getLiteral(EC_NAME).getString());
            }
        }
        assertEquals(protCnt, protSet.size());
        assertEquals(fullNameCnt, fullNameSet.size());
        assertEquals(shortNameCnt, shortNameSet.size());
        assertEquals(ecNameCnt, ecNameSet.size());
    }
}