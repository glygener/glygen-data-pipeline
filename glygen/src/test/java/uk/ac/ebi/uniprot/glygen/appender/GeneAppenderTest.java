package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.ALT_LABEL;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.COUNT;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PREFIX_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getDefaultTestOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.writeOutputModel;

class GeneAppenderTest {
    private Model rdfModel = getTestRdfModel();
    private GeneAppender appender;

    @BeforeEach
    void init() {
        GlygenConfig config = new GlygenConfig();
        config.setTaxId("9606");
        config.setGeneInfo("tst_9606_gene.tsv");

        appender = new GeneAppender(config);
    }

    @Test
    void testAppendDataForGene() {
        Model outModel = getDefaultTestOutModel();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        writeOutputModel(outModel);

        String queryStr ="select (count(*) as ?count) { " +
                "       ?protein <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  " +
                "               <http://purl.uniprot.org/core/Protein> . " +
                "       FILTER EXISTS { ?protein <http://purl.uniprot.org/core/encodedBy> ?gene . } } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(5, solution.getLiteral(COUNT).getInt());
    }

    @Test
    void testAppendDataForGeneLabels() {
        Model outModel = ModelFactory.createDefaultModel();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        // all genes have prefLabel ?
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                " select (count(*) as ?count) { " +
                "   ?gene rdf:type <http://purl.uniprot.org/core/Gene> . " +
                "   ?gene skos:prefLabel ?prefLabel . " +
                "   OPTIONAL { ?gene up:orfName ?orfName . } }";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(5, solution.getLiteral(COUNT).getInt());
    }


    @Test
    void testAppendDataForGeneAltLabels() {
        Model outModel = ModelFactory.createDefaultModel();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        // only one gene has 2 altLabel ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "   ?gene rdf:type <http://purl.uniprot.org/core/Gene> . " +
                "   ?gene skos:altLabel ?altLabel . } ";

        Set<String> altLabelSet = new HashSet<>();
        Set<String> geneSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            altLabelSet.add(solution.getLiteral(ALT_LABEL).getString());
            geneSet.add(solution.getResource("gene").getURI());
        }
        assertEquals(1, geneSet.size());
        assertEquals(2, altLabelSet.size());
    }
}