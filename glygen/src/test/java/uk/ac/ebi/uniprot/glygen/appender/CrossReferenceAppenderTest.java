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
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.DATABASE;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.RESOURCE;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class CrossReferenceAppenderTest {

    @Test
    void testAppendDataForDbCrossReferences() {
        GlygenConfig.setDatabases("in/testDatabases.rdf");

        Model outModel = getDefaultTestOutModel();
        GlygenDataset dataset = new GlygenDataset(getTestRdfModel(), outModel);

        CrossReferenceAppender appender = new CrossReferenceAppender();
        appender.appendData(dataset);

        // all cross references are accounted ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein rdfs:seeAlso ?resource . " +
                "       ?resource rdf:type <http://purl.uniprot.org/core/Resource> . " +
                "       ?resource up:database ?database . }";

        Set<String> dbSet = new HashSet<>();
        Set<String> protSet = new HashSet<>();
        Set<String> resSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            protSet.add(solution.getResource(PROTEIN).getURI());
            dbSet.add(solution.getResource(DATABASE).getURI());
            resSet.add(solution.getResource(RESOURCE).getURI());
        }
        assertEquals(5, protSet.size());
        assertEquals(95, dbSet.size());
        assertEquals(476, resSet.size());
    }
}