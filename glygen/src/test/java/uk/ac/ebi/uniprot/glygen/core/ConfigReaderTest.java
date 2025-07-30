package uk.ac.ebi.uniprot.glygen.core;

import org.junit.jupiter.api.Test;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigReaderTest {

    @Test
    void testGetGlygenConfigList() throws IOException {
        List<GlygenConfig> configList =
                ConfigReader.getGlygenConfigList(getClass().getResourceAsStream("/tstGlygenConfig.properties"), "");
        assertEquals(1, configList.size());
        GlygenConfig config = configList.get(0);
        assertEquals("homo-sapiens", config.getName());
        assertEquals("in/dbHomo", config.getDbDir());
        assertEquals("in/UP000005640_9606.fasta", config.getCanonical());
        assertEquals("in/UP000005640_9606_additional.fasta", config.getIsoform());
        assertEquals("in/test.coordinates.json", config.getGeneCoordinate());
        assertEquals("in/Homo_sapiens.cds.all.fa", config.getEnsemblCds());
        assertEquals("in/Homo_sapiens.pep.all.fa", config.getEnsemblPeptide());
        assertEquals("Ensembl", config.getTranscriptDb());
        assertEquals("uniprot-proteome-homo-sapiens.nt", config.getRdfOutput());
        assertEquals("in/testTissues.rdf", GlygenConfig.getTissues());
        assertEquals("in/testKeywords.rdf", GlygenConfig.getKeywords());
        assertEquals("in/testGo.owl", GlygenConfig.getGeneOntologies());
        assertEquals("in/testEnzyme.rdf", GlygenConfig.getEnzyme());
        assertEquals("in/testDatabases.rdf", GlygenConfig.getDatabases());
        assertEquals("in/testDiseases.rdf", GlygenConfig.getDiseases());
        assertEquals("in/testLocations.rdf", GlygenConfig.getLocations());
        assertEquals("UP000005640", config.getUpid());
        assertEquals("statistics-homo-sapiens.json", config.getStatisticsFile());
        assertEquals("9606", config.getTaxId());
        assertEquals("in/testDbSnp.xml", config.getDbSnp());
        assertEquals("dbSNP-homo-sapiens.tsv", config.getDbSnpOut());
        assertEquals("in/homo_sapiens.txt", config.getReactomeReactions());
    }


    @Test
    void testCopyCommonProperties() throws IOException {
        List<GlygenConfig> configList =
                ConfigReader.getGlygenConfigList(getClass().getResourceAsStream("/tstGlygenConfig.properties"), "");
        assertEquals(1, configList.size());
        assertNotNull(GlygenConfig.getEnzyme());
        assertNotNull(GlygenConfig.getTissues());
        assertNotNull(GlygenConfig.getKeywords());
        assertNotNull(GlygenConfig.getGeneOntologies());
        assertNotNull(GlygenConfig.getDatabases());
        assertNotNull(GlygenConfig.getDiseases());
        assertNotNull(GlygenConfig.getLocations());
    }

    @Test
    void testGlygenConfigListInvalidFile() {
        assertThrows(NullPointerException.class, () -> ConfigReader.getGlygenConfigList(getClass().getResourceAsStream
                ("InvalidFileName"), ""));
    }
}