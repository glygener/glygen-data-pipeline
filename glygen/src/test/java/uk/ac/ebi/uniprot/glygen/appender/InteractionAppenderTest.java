package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.EXPERIMENTS;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PARTICIPANT;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.XENO;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PREFIX_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_PROTEIN_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestResourcePath;

public class InteractionAppenderTest {
    private Model rdfModel = getTestRdfModel();

    private Model getTestModel() {
        GlygenConfig.setIntAct(getTestResourcePath("in/testIntAct.txt"));
        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);

        BasicInfoAppender appender = new BasicInfoAppender();
        appender.appendData(dataset);

        InteractionAppender intAppender = new InteractionAppender(new GlygenConfig());
        intAppender.appendData(dataset);
        return outModel;
    }

    @Test
    void testAppendDataForInteraction() {
        Model outModel = getTestModel();

        String queryStr = SPARQL_QUERY_PREFIX_STR +
                " select distinct ?interaction ?xeno ?experiments where {" +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:interaction ?interaction . " +
                "       ?interaction up:xeno ?xeno . " +
                "       ?interaction up:experiments ?experiments . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(XENO));
            assertTrue(solution.contains(EXPERIMENTS));
            ++count;
        }
        assertEquals(461, count);
    }

    @Test
    void testSelfInteractionHasOnlyOneParticipant() {
        Model outModel = getTestModel();

        String queryStr = SPARQL_QUERY_PROTEIN_STR +
                "       ?protein up:interaction ?interaction . " +
                "       ?interaction rdf:type <http://purl.uniprot.org/core/Self_Interaction> . " +
                "       ?interaction up:participant ?participant . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(PARTICIPANT));
            ++count;
        }
        assertEquals(4, count);
    }

    @Test
    void testNonSelfInteractionHasMoreThanOneParticipant() {
        Model outModel = getTestModel();

        String queryStr = SPARQL_QUERY_PREFIX_STR +
                " select ?protein ?participant where {" +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:interaction ?interaction . " +
                "       ?interaction rdf:type <http://purl.uniprot.org/core/Non_Self_Interaction> . " +
                "       ?interaction up:participant ?participant . } ";

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            assertTrue(solution.contains(PARTICIPANT));
            ++count;
            System.out.println(solution.getResource("protein").getURI() + " " + solution.getResource("participant").getURI());
        }
        assertEquals(914, count);
    }

    @Test
    void testAppendDataForParticipant() {
        Model outModel = getTestModel();
        String queryStr =  SPARQL_QUERY_PREFIX_STR +
                " select distinct ?participant ?sameAs where {" +
                "       ?interaction rdf:type <http://purl.uniprot.org/core/Interaction> . " +
                "       ?interaction up:participant ?participant . " +
                "       ?participant rdf:type <http://purl.uniprot.org/core/Participant> . " +
                "       ?participant owl:sameAs ?sameAs . }" ;

        ResultSet resultSet = getResultSet(outModel, queryStr);
        int count = 0;
        while (resultSet.hasNext()) {
            resultSet.nextSolution();
            ++count;
        }
        assertEquals(425, count);
    }
}
