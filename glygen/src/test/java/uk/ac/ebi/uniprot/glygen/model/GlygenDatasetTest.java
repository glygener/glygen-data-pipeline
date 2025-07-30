package uk.ac.ebi.uniprot.glygen.model;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlygenDatasetTest {
    private Model rdfModel = ModelFactory.createDefaultModel();
    private Model outModel = ModelFactory.createDefaultModel();

    private GlygenDataset createNewDataset() {
        return new GlygenDataset(rdfModel, outModel);
    }

    @Test
    void testGlygenDatasetConstructor() {
        GlygenDataset dataset = createNewDataset();

        assertEquals(rdfModel, dataset.getRdfModel());
        assertEquals(outModel, dataset.getOutModel());
        assertEquals(0, dataset.getCanonicalCount());
        assertEquals(0, dataset.getAccessionSet().size());
    }

    @Test
    void testCreateAccessionMapCreatesNewMap() {
        String acc1 = "http://purl.uniprot.org/uniprot/P05067";
        String acc2 = "http://purl.uniprot.org/uniprot/P04843";
        String acc3 = "http://purl.uniprot.org/uniprot/A0A0C4DGS1";
        GlygenDataset dataset = createNewDataset();
        Set<String> accSet1 = new HashSet<>();
        accSet1.add(acc1);
        accSet1.add(acc2);
        dataset.createAccessionMap(accSet1);

        assertEquals(2, dataset.getAccessionSet().size());
        assertTrue(dataset.getAccessionSet().contains(acc1));
        assertTrue(dataset.getAccessionSet().contains(acc2));

        Set<String> accSet2 = new HashSet<>();
        accSet2.add(acc3);
        dataset.createAccessionMap(accSet2);

        assertEquals(1, dataset.getAccessionSet().size());
        assertFalse(dataset.getAccessionSet().contains(acc1));
        assertFalse(dataset.getAccessionSet().contains(acc2));
        assertTrue(dataset.getAccessionSet().contains(acc3));
    }

    @Test
    void testExceptionOnAddingSameCanonicalUsingIsoformId() {
        String isoId = "http://purl.uniprot.org/isoforms/P05067-1";
        String acc = "http://purl.uniprot.org/uniprot/P05067";

        GlygenDataset dataset = createNewDataset();
        Set<String> set = new HashSet<>();
        set.add(acc);
        dataset.createAccessionMap(set);

        dataset.addIsoformIdToCanonicalSet(isoId);

        assertThrows(GlyGenException.class, () -> dataset.addIsoformIdToCanonicalSet(isoId));
    }

    @Test
    void testExceptionOnInvalidIsoformId() {
        String isoId = "http://purl.uniprot.org/isoforms/P05067";
        GlygenDataset dataset = createNewDataset();
        assertThrows(GlyGenException.class, () -> dataset.addIsoformIdToCanonicalSet(isoId));
    }

    @Test
    void testExceptionOnAccessionMissingInDataset() {
        String isoId = "http://purl.uniprot.org/isoforms/P05067-1";
        GlygenDataset dataset = createNewDataset();
        assertThrows(GlyGenException.class, () -> dataset.addIsoformIdToCanonicalSet(isoId));
    }

    @Test
    void testExceptionOnAddingCanonicalTwice() {
        String isoId = "http://purl.uniprot.org/isoforms/P05067-1";
        GlygenDataset dataset = createNewDataset();
        Set<String> set = new HashSet<>();
        set.add("http://purl.uniprot.org/uniprot/P05067");
        dataset.createAccessionMap(set);
        dataset.addIsoformIdToCanonicalSet(isoId);
        assertThrows(GlyGenException.class, () -> dataset.addIsoformIdToCanonicalSet(isoId));
    }
}