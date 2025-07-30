package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultClassificationAppender;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class ClassificationAppenderTest {
    private final Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForClassifications() {
        String acc1 = "http://purl.uniprot.org/uniprot/B3KU38";

        Model outModel = ModelFactory.createDefaultModel();
        Resource protein = outModel.createResource(acc1);
        protein.addProperty(RDF.type, outModel.createResource(UP_TYPE_PROTEIN));

        Set<String> accSet = new HashSet<>();
        accSet.add(acc1);

        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(accSet);

        ClassificationAppender appender = getDefaultClassificationAppender();
        appender.appendData(dataset);

        // all keywords(up:concept) are accounted & have skos:prefLabel & skos:altLabel ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?keyword rdf:type <http://purl.uniprot.org/core/Concept> . " +
                "       ?keyword skos:prefLabel ?prefLabel . " +
                "       OPTIONAL {?keyword skos:altLabel ?altLabel } }";

        Set<String> keywordSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        int altCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            keywordSet.add(solution.getResource("keyword").getURI());
            assertNotNull(solution.getLiteral(PREF_LABEL));
            if (solution.contains(ALT_LABEL)) {
                ++altCount;
            }
        }
        assertEquals(5, keywordSet.size());
        assertEquals(3, altCount);

        // all go classifications (owl:Class) are accounted & have rdfs:label ?
        queryStr = SPARQL_QUERY_BASE_STR +
                "       ?goClass rdf:type <http://www.w3.org/2002/07/owl#Class> . " +
                "       ?goClass rdfs:label ?label . } ";

        Set<String> goSet = new HashSet<>();
        resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            goSet.add(solution.getResource("goClass").getURI());
            assertNotNull(solution.getLiteral(LABEL));
        }
        assertEquals(1, goSet.size());


        // correct protein to classified mapping ?
        queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:classifiedWith ?keyword . } ";

        resultSet = getResultSet(outModel, queryStr);
        Set<String> keySet = new HashSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            keySet.add(solution.getResource("keyword").getURI());
        }
        assertEquals(9, keySet.size());
    }

    @Test
    void testExceptionOnMissingKeywordsRdfFile() {
        GlygenConfig.setKeywords("in/keywords1.rdf");
        GlygenConfig.setGeneOntologies("in/testGo.owl");

        assertThrows(RuntimeException.class, ClassificationAppender::new);
    }

    @Test
    void testExceptionOnMissingGeneOntologiesOwlFile() {
        GlygenConfig.setKeywords("in/testKeywords.rdf");
        GlygenConfig.setGeneOntologies("in/testGo1.owl");

        assertThrows(RuntimeException.class, ClassificationAppender::new);
    }

    @Test
    void testExceptionOnMissingKeywordInKeywordsRdf() {
        Model rdfModel = ModelFactory.createDefaultModel();
        Resource protein = rdfModel.createResource("http://purl.uniprot.org/uniprot/P05067");
        protein.addProperty(RDF.type, rdfModel.createResource(UP_TYPE_PROTEIN));
        protein.addProperty(rdfModel.createProperty(UP_CLASSIFIED_WITH),
                rdfModel.createResource("http://purl.uniprot.org/keywords/26"));

        Model outModel = ModelFactory.createDefaultModel();

        ClassificationAppender appender = getDefaultClassificationAppender();
        assertThrows(RuntimeException.class, () -> appender.appendData(new GlygenDataset(rdfModel, outModel)));
    }

    @Test
    void testNoExceptionOnObsoleteClassificationInGoOwl() {
        Model rdfModel = ModelFactory.createDefaultModel();
        Resource protein = rdfModel.createResource("http://purl.uniprot.org/uniprot/P05067");
        protein.addProperty(RDF.type, rdfModel.createResource(UP_TYPE_PROTEIN));
        protein.addProperty(rdfModel.createProperty(UP_CLASSIFIED_WITH),
                rdfModel.createResource("http://purl.obolibrary.org/obo/GO_0005748"));

        Model outModel = ModelFactory.createDefaultModel();

        ClassificationAppender appender = getDefaultClassificationAppender();
        assertDoesNotThrow(() -> appender.appendData(new GlygenDataset(rdfModel, outModel)));
    }
}