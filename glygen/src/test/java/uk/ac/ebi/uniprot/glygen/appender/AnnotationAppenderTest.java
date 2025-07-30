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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.ANNOTATION_TYPES;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PREFIX_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.addProteinToOutModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class AnnotationAppenderTest {

    private Model rdfModel = getTestRdfModel();
    private Model outModel;
    private Set<String> accSet = new HashSet<>();

    @Test
    void testAppendDataForAnnotations() {
        appendTestData();
        // all proteins have annotations?
        String queryStr = SPARQL_QUERY_PREFIX_STR +
                "   SELECT DISTINCT ?protein WHERE { " +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       FILTER EXISTS { ?protein up:annotation ?annotation } }";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int protCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(accSet.contains(solution.getResource(PROTEIN).getURI()));
            ++protCount;
        }
        assertEquals(4, protCount);
    }

    @Test
    void testOnlyListedAnnotationsAreAppended() {
        appendTestData();
        // only required annotations listed in ANNOTATION_TYPES are appended?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:annotation ?annotation . " +
                "       ?annotation rdf:type ?type . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int annCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(ANNOTATION_TYPES.contains(solution.getResource(TYPE).getURI()));
            ++annCount;
        }
        assertEquals(280, annCount);
    }

    @Test
    void testPositionInfoAppended() {
        appendTestData();
        String queryStr ="select (count(*) as ?count) { " +
                "       ?position <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>" +
                "        <http://biohackathon.org/resource/faldo#ExactPosition> . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(224, solution.getLiteral(COUNT).getInt());
    }

    @Test
    void testGlycosylationInfoAppended() {
        appendTestData();
        String queryStr = SPARQL_QUERY_PREFIX_STR + "  prefix gly: <https://sparql.glygen.org/ontology/> " +
                " select * where {" +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Glycosylation_Annotation> . " +
                "       ?annotation rdfs:comment ?comment . " +
                "       ?annotation gly:attribution ?attribution . " +
                "       ?attribution up:source ?source . " +
                "       ?attribution up:evidence ?evidence . " +
                "       ?annotation up:range ?range . " +
                "       ?range faldo:begin ?begin . " +
                "       ?range faldo:end ?end . }" ;

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int glyCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(COMMENT));
            assertTrue(solution.contains(SOURCE));
            assertTrue(solution.contains(EVIDENCE));
            assertTrue(solution.contains(BEGIN));
            assertTrue(solution.contains(END));
            ++glyCount;
        }
        assertEquals(14, glyCount);
    }

    @Test
    void testDiseaseInfoAppended() {
        appendTestData();
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?disease rdf:type <http://purl.uniprot.org/core/Disease> . " +
                "       ?disease skos:prefLabel ?prefLabel . " +
                "       ?disease rdfs:comment ?comment . " +
                "       ?disease up:mnemonic ?mnemonic . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int disCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(PREF_LABEL));
            assertTrue(solution.contains(COMMENT));
            assertTrue(solution.contains(MNEMONIC));
            ++disCount;
        }
        assertEquals(2, disCount);
    }

    @Test
    void testCellularComponentInfoAppended() {
        appendTestData();
        String queryStr = "select (count(*) as ?count) { " +
                "       ?location <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>" +
                "        <http://purl.uniprot.org/core/Cellular_Component> . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        assertTrue(resultSet.hasNext());
        QuerySolution solution = resultSet.nextSolution();
        assertEquals(3, solution.getLiteral(COUNT).getInt());
    }

    @Test
    void testCatalyticActivityAtribution() {
        appendTestData();
        String queryStr = SPARQL_QUERY_PREFIX_STR + "  prefix gly: <https://sparql.glygen.org/ontology/> " +
                " select * where {" +
                "       ?activity rdf:type <http://purl.uniprot.org/core/Catalytic_Activity> . " +
                "       ?activity up:catalyzedReaction ?catalyzedReaction . " +
                "       ?activity gly:attribution ?attribution . " +
                "       ?attribution up:evidence ?evidence . " +
                "       OPTIONAL { ?attribution up:source ?source . } }" ;


        ResultSet resultSet = getResultSet(outModel, queryStr);
        int attribCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(CATALYZED_REACTION));
            assertTrue(solution.contains(EVIDENCE));
            ++attribCount;
        }
        assertEquals(2, attribCount);
    }

    @Test
    void testCatalyticActivityAnnotationAttributes() {
        appendTestData();
        String queryStr = SPARQL_QUERY_PREFIX_STR + "  prefix gly: <https://sparql.glygen.org/ontology/> " +
                " select * where {" +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Catalytic_Activity_Annotation> . " +
                "       ?annotation up:catalyticActivity ?catalyticActivity . " +
                "       OPTIONAL { ?annotation gly:catalyzedPhysiologicalActivity ?activity . }" +
                " }";


        ResultSet resultSet = getResultSet(outModel, queryStr);
        int actCount = 0;
        int reactCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(CATALYTIC_ACTIVITY));
            if (solution.contains(ACTIVITY)) {
                reactCount++;
            }
            actCount++;
        }
        assertEquals(1, actCount);
        assertEquals(1, reactCount);
    }

    @Test
    void testExceptionOnMissingDiseasesRdfFile() {
        GlygenConfig config = new GlygenConfig();
        config.setDiseases("in/testDiseases1.rdf");
        config.setLocations("in/testLocations.rdf");
        config.setRhea("in/testRhea.rdf");
        assertThrows(RuntimeException.class, () -> new AnnotationAppender(config));
    }

    @Test
    void testExceptionOnMissingLocationsRdfFile() {
        GlygenConfig config = new GlygenConfig();
        config.setDiseases("in/testDiseases.rdf");
        config.setLocations("in/testLocations1.rdf");
        config.setRhea("in/testRhea.rdf");
        assertThrows(RuntimeException.class, () -> new AnnotationAppender(config));
    }

    @Test
    void testExceptionOnMissingRheaRdfFile() {
        GlygenConfig config = new GlygenConfig();
        config.setDiseases("in/testDiseases.rdf");
        config.setLocations("in/testLocations.rdf");
        config.setRhea("in/testRhea1.rdf");
        assertThrows(RuntimeException.class, () -> new AnnotationAppender(config));
    }

    private void appendTestData() {
        if (outModel == null) {
            GlygenConfig config = new GlygenConfig();
            config.setDiseases("in/testDiseases.rdf");
            config.setLocations("in/testLocations.rdf");
            config.setRhea("in/testRhea.rdf");

            String acc1 = "http://purl.uniprot.org/uniprot/P05067";
            String acc2 = "http://purl.uniprot.org/uniprot/A0A0A0MRG2";
            String acc3 = "http://purl.uniprot.org/uniprot/B3KU38";
            String acc4 = "http://purl.uniprot.org/uniprot/A0A0J9YW22";
            String acc5 = "http://purl.uniprot.org/uniprot/Q3LFU0";

            outModel = ModelFactory.createDefaultModel();
            addProteinToOutModel(outModel, acc1);
            addProteinToOutModel(outModel, acc2);
            addProteinToOutModel(outModel, acc3);
            addProteinToOutModel(outModel, acc4);
            addProteinToOutModel(outModel, acc5);

            accSet.add(acc1);
            accSet.add(acc2);
            accSet.add(acc3);
            accSet.add(acc4);
            accSet.add(acc5);

            GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
            dataset.createAccessionMap(accSet);

            AnnotationAppender appender = new AnnotationAppender(config);
            appender.appendData(dataset);
        }
    }

    @Test
    void testLigandInfoAppended() {
        appendTestData();
        String queryStr = SPARQL_QUERY_PREFIX_STR + "  prefix gly: <https://sparql.glygen.org/ontology/> " +
                " select * where {" +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Binding_Site_Annotation> . " +
                "       ?annotation up:ligand ?ligand . " +
                "       ?ligand rdfs:subClassOf ?subClassOf . " +
                "       ?annotation up:range ?range . " +
                "       ?range faldo:begin ?begin . " +
                "       ?range faldo:end ?end . " +
                "       OPTIONAL { ?ligand rdfs:label ?label . } " +
                "       OPTIONAL { ?ligand rdfs:comment ?comment . } }" ;

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int ligCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(LIGAND));
            assertTrue(solution.contains(SUB_CLASS_OF));
            assertTrue(solution.contains(RANGE));
            assertTrue(solution.contains(BEGIN));
            assertTrue(solution.contains(END));
            ++ligCount;
        }
        assertEquals(17, ligCount);
    }

    @Test
    void testLigandPartInfoAppended() {
        appendTestData();
        String queryStr = SPARQL_QUERY_PREFIX_STR + "  prefix gly: <https://sparql.glygen.org/ontology/> " +
                " select * where {" +
                "       ?annotation rdf:type <http://purl.uniprot.org/core/Binding_Site_Annotation> . " +
                "       ?annotation up:ligandPart ?ligandPart . " +
                "       ?ligandPart rdfs:subClassOf ?subClassOf . " +
                "       ?annotation up:range ?range . " +
                "       ?range faldo:begin ?begin . " +
                "       ?range faldo:end ?end . " +
                "       OPTIONAL { ?ligandPart rdfs:label ?label . } " +
                "       OPTIONAL { ?ligandPart rdfs:comment ?comment . } }" ;

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int ligCount = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(LIGAND_PART));
            assertTrue(solution.contains(SUB_CLASS_OF));
            assertTrue(solution.contains(RANGE));
            assertTrue(solution.contains(BEGIN));
            assertTrue(solution.contains(END));
            ++ligCount;
        }
        assertEquals(1, ligCount);
    }
}