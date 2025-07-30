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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.LABEL;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UP_ISOLATED_FROM;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UP_TYPE_PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.SPARQL_QUERY_BASE_STR;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getResultSet;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class TissueAppenderTest {
    private final Model rdfModel = getTestRdfModel();

    @Test
    void testAppendDataForTissues() {
        GlygenConfig config = new GlygenConfig();
        GlygenConfig.setTissues("in/testTissues.rdf");

        String acc1 = "http://purl.uniprot.org/uniprot/B3KU38";
        String acc2 = "http://purl.uniprot.org/uniprot/A0A0A0MRG2";

        Model outModel = ModelFactory.createDefaultModel();
        Resource protein = outModel.createResource(acc1);
        protein.addProperty(RDF.type, outModel.createResource(UP_TYPE_PROTEIN));

        protein = outModel.createResource(acc2);
        protein.addProperty(RDF.type, outModel.createResource(UP_TYPE_PROTEIN));

        Set<String> accSet = new HashSet<>();
        accSet.add(acc1);
        accSet.add(acc2);

        GlygenDataset dataset = new GlygenDataset(rdfModel, outModel);
        dataset.createAccessionMap(accSet);

        TissueAppender appender = new TissueAppender(config);
        appender.appendData(dataset);

        // all tissues are accounted & have rdfs:label ?
        String queryStr = SPARQL_QUERY_BASE_STR +
                "       ?tissue rdf:type <http://purl.uniprot.org/core/Tissue> . " +
                "       ?tissue rdfs:label ?label . } ";

        Set<String> tissueSet = new HashSet<>();
        ResultSet resultSet = getResultSet(outModel, queryStr);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            tissueSet.add(solution.getResource("tissue").getURI());
            assertNotNull(solution.getLiteral(LABEL));
        }
        assertEquals(7, tissueSet.size());

        // correct protein to tissue mapping ?
        queryStr = SPARQL_QUERY_BASE_STR +
                "       ?protein rdf:type <http://purl.uniprot.org/core/Protein> . " +
                "       ?protein up:isolatedFrom ?tissue . } ";

        resultSet = getResultSet(outModel, queryStr);
        Set<String> protSet = new HashSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            protSet.add(solution.getResource(PROTEIN).getURI());
        }
        assertEquals(1, protSet.size());
    }

    @Test
    void testExceptionOnMissingTissuesRdfFile() {
        GlygenConfig config = new GlygenConfig();
        GlygenConfig.setTissues("in/tissue1.rdf");

        assertThrows(RuntimeException.class, () -> new TissueAppender(config));
    }

    @Test
    void testExceptionOnMissingTissueIdInTissuesRdf() {
        GlygenConfig.setTissues("in/testTissues.rdf");

        Model rdfModel = ModelFactory.createDefaultModel();
        Resource protein = rdfModel.createResource("http://purl.uniprot.org/uniprot/P05067");
        protein.addProperty(RDF.type, rdfModel.createResource(UP_TYPE_PROTEIN));
        protein.addProperty(rdfModel.createProperty(UP_ISOLATED_FROM),
                rdfModel.createResource("http://purl.uniprot.org/tissues/137"));

        Model outModel = ModelFactory.createDefaultModel();

        TissueAppender appender = new TissueAppender(new GlygenConfig());
        assertThrows(RuntimeException.class, () -> appender.appendData(new GlygenDataset(rdfModel, outModel)));
    }
}