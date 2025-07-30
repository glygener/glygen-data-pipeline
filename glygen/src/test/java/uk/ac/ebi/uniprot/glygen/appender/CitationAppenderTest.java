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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.writeOutputModel;

class CitationAppenderTest {
    private Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForCitations() {
        Model outModel = getDefaultTestOutModel();

        CitationAppender appender = new CitationAppender();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        writeOutputModel(outModel);
        // all proteins have citations?
        String queryStr ="select (count(*) as ?count) { " +
                "       ?protein <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  " +
                "               <http://purl.uniprot.org/core/Protein> . " +
                "       FILTER EXISTS { ?protein <http://purl.uniprot.org/core/citation> ?citation . } } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(5, solution.getLiteral(COUNT).getInt());
    }

    @Test
    void testAppendDataForCitationData() {
        Model outModel = ModelFactory.createDefaultModel();

        CitationAppender appender = new CitationAppender();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        // all citations have all fields listed?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?citation rdf:type <http://purl.uniprot.org/core/Journal_Citation> . " +
                "       ?citation up:title ?title . " +
                "       ?citation skos:exactMatch ?exactMatch . " +
                //"       ?citation <http://purl.org/dc/terms#identifier> ?identifier . " +
                "       ?citation up:volume ?volume . " +
                "       ?citation up:pages ?pages . " +
                "       ?citation up:date ?date . " +
                "       ?citation up:name ?name . } ";

        Set<String> citSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            citSet.add(solution.getResource(CITATION).getURI());
            assertNotNull(solution.getLiteral(TITLE));
            assertNotNull(solution.getResource(EXACT_MATCH));
            //assertNotNull(solution.getLiteral(IDENTIFIER));
            assertNotNull(solution.getLiteral(VOLUME));
            assertNotNull(solution.getLiteral(PAGES));
            assertNotNull(solution.getLiteral(NAME));
        }
        assertEquals(173, citSet.size());
    }

    @Test
    void testAppendDataForCitationAuthors() {
        Model outModel = ModelFactory.createDefaultModel();

        CitationAppender appender = new CitationAppender();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        String queryStr = SPARQL_QUERY_BASE_STR + " <http://purl.uniprot.org/citations/14702039> up:author ?author . }";

        Set<String> authSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            authSet.add(solution.getLiteral(AUTHOR).getString());
        }
        assertEquals(157, authSet.size());
    }

}