package uk.ac.ebi.uniprot.glygen.util;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestResourcePath;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class RdfUtilityTest {
    private Model rdfModel = null;

    @BeforeAll
    void setUp() {
        if (rdfModel == null) {
            String rdfFile = getTestResourcePath(("in/testRdf.rdf"));
            rdfModel = ModelFactory.createDefaultModel();
            InputStream in = FileManager.get().open(rdfFile);
            if (in == null) {
                throw new IllegalArgumentException("File: " + rdfFile + " not found");
            }
            rdfModel.read(in, null);
        }
    }

    @Test
    void getOntModelBase() {
        assertNotNull(RdfUtility.getOntModelBase(null));
        Map<String, String> prefMap = RdfUtility.getOntModelBase(null).getNsPrefixMap();
        assertEquals(prefMap.size(), 8);
        assertTrue(prefMap.containsKey("owl"));
        assertTrue(prefMap.containsKey("rdf"));
        assertTrue(prefMap.containsKey("rdfs"));
        assertTrue(prefMap.containsKey("up"));
        assertTrue(prefMap.containsKey("xsd"));
        assertTrue(prefMap.containsKey("owl"));
        assertTrue(prefMap.containsKey("faldo"));
        assertTrue(prefMap.containsKey("gly"));
    }

    @Test
    void getCanonicalSequenceIdFromRDF() {
        // sp entry
        assertEquals("P05067-1", RdfUtility.getCanonicalSequenceIdFromRdf(rdfModel, "P05067"));
        // tr entry
        assertEquals("A0A0A0MRG2-1", RdfUtility.getCanonicalSequenceIdFromRdf(rdfModel, "A0A0A0MRG2"));
    }

    @Test
    void getXrefsFromRDF() {
        ResultSet resultSet = getDbCrossReferenceFromRdf(rdfModel);
        Set<String> xrefs = new TreeSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String uri = solution.getResource(DATABASE).getURI();
            xrefs.add(uri.substring(uri.lastIndexOf('/') + 1));
        }
        for (String xref : xrefs) {
            System.out.println(xref);
        }
        assertEquals(95, xrefs.size());
    }

    @Test
    void getDistinctXrefDbsFromRdf() {
        ResultSet resultSet = getDistinctDbCrossRefDatabasesFromRdf(rdfModel);
        Set<String> dbs = new TreeSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String uri = solution.getResource(DATABASE).getURI();
            dbs.add(uri.substring(uri.lastIndexOf('/') + 1));
        }
        for (String xref : dbs) {
            System.out.println(xref);
        }
        assertEquals(99, dbs.size());
    }


}