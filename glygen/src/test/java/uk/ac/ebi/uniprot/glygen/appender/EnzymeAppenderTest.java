package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
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

class EnzymeAppenderTest {
    private Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForEnzymes() {
        GlygenConfig.setEnzyme("in/testEnzyme.rdf");

        Model outModel = getDefaultTestOutModel();

        EnzymeAppender appender = new EnzymeAppender();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        writeOutputModel(outModel);

        String queryStr ="select (count(*) as ?count) { " +
                "       ?protein <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  " +
                "               <http://purl.uniprot.org/core/Protein> . " +
                "       FILTER EXISTS { ?protein <http://purl.uniprot.org/core/enzyme> ?enzyme . } } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(1, solution.getLiteral(COUNT).getInt());
    }

    @Test
    void testAppendDataForEnzymeData() {
        GlygenConfig.setEnzyme("in/testEnzyme.rdf");

        Model outModel = ModelFactory.createDefaultModel();

        EnzymeAppender appender = new EnzymeAppender();
        appender.appendData(new GlygenDataset(rdfModel, outModel));

        // all enzymes have all fields listed?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?enzyme rdf:type <http://purl.uniprot.org/core/Enzyme> . " +
                "       ?enzyme up:activity ?activity . " +
                "       ?enzyme skos:prefLabel ?prefLabel . " +
                "       OPTIONAL { ?enzyme up:cofactorLabel ?cofactorLabel . } " +
                "       OPTIONAL { ?enzyme up:obsolete ?obsolete . } " +
                "       OPTIONAL { ?enzyme up:replacedBy ?replacedBy . } " +
                "       OPTIONAL { ?enzyme up:replaces ?replaces . } " +
                "       OPTIONAL { ?enzyme rdfs:subClassOf ?subClassOf . } " +
                "       OPTIONAL { ?enzyme skos:altLabel ?altLabel . } }";

        Set<String> altLabelSet = new HashSet<>();
        Set<String> prefLabelSet = new HashSet<>();
        Set<String> enzSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            altLabelSet.add(solution.getLiteral(ALT_LABEL).getString());
            prefLabelSet.add(solution.getLiteral(PREF_LABEL).getString());
            enzSet.add(solution.getResource(ENZYME).getURI());
            assertNotNull(solution.getResource(ACTIVITY));
            assertNotNull(solution.getResource(SUB_CLASS_OF));
        }
        assertEquals(2, enzSet.size());
        assertEquals(2, prefLabelSet.size());
        assertEquals(8, altLabelSet.size());
    }
}