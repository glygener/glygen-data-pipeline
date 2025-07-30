package uk.ac.ebi.uniprot.glygen.model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlygenConfigTest {
    private final GlygenConfig config = new GlygenConfig();

    @Test
    void testName() {
        String name = "name_1";
        assertNull(config.getName());
        config.setName(name);
        assertEquals(name, config.getName());
    }

    @Test
    void testDbDir() {
        String dbDir = "in/dbNov2017";
        assertNull(config.getDbDir());
        config.setDbDir(dbDir);
        assertEquals(dbDir, config.getDbDir());
    }

    @Test
    void testDbDirOut() {
        String dbDir = "in/dbNov2017";
        config.setDbDir(dbDir);
        assertEquals(dbDir + "out", config.getDbDirOut());
    }

    @Test
    void testCanonical() {
        String can = "in/UP000000589_10090.fasta";
        assertNull(config.getCanonical());
        config.setCanonical(can);
        assertEquals(can, config.getCanonical());
    }

    @Test
    void testIsoform() {
        String iso = "in/UP000000589_10090_additional.fasta";
        assertNull(config.getIsoform());
        config.setIsoform(iso);
        assertEquals(iso, config.getIsoform());
    }

    @Test
    void testGeneCoordinate() {
        String coor = "in/UP000000589_10090.coordinates.json";
        assertNull(config.getGeneCoordinate());
        config.setGeneCoordinate(coor);
        assertEquals(coor, config.getGeneCoordinate());
    }

    @Test
    void testEnsemblCds() {
        String cds = "in/Mus_musculus.GRCm38.75.cds.all";
        assertNull(config.getEnsemblCds());
        config.setEnsemblCds(cds);
        assertEquals(cds, config.getEnsemblCds());
    }

    @Test
    void testEnsemblPeptide() {
        String peptide = "in/Mus_musculus.GRCm38.75.pep.all";
        assertNull(config.getEnsemblPeptide());
        config.setEnsemblPeptide(peptide);
        assertEquals(peptide, config.getEnsemblPeptide());
    }

    @Test
    void testTranscriptDb() {
        String tdb = "EnsemblMetazoa";
        assertNull(config.getTranscriptDb());
        config.setTranscriptDb(tdb);
        assertEquals(tdb, config.getTranscriptDb());
    }

    @Test
    void testTissues() {
        String tissues = "in/testTissues.rdf";
        GlygenConfig.setTissues(tissues);
        assertEquals(tissues, GlygenConfig.getTissues());
    }

    @Test
    void testEnzyme() {
        String enzyme = "in/testEnzyme.rdf";
        GlygenConfig.setEnzyme(enzyme);
        assertEquals(enzyme, GlygenConfig.getEnzyme());
    }

    @Test
    void testDatabases() {
        String databases = "in/testDatabases.rdf";
        GlygenConfig.setDatabases(databases);
        assertEquals(databases, GlygenConfig.getDatabases());
    }

    @Test
    void testIntAct() {
        GlygenConfig.setIntAct("in/testIntAct.txt");
        assertNotNull(GlygenConfig.getSameAsAttributeForIntActId("124"));
    }

    @Test
    void testGeneOntologies() {
        String geneOntologies = "in/testGo.owl";
        GlygenConfig.setGeneOntologies(geneOntologies);
        assertEquals(geneOntologies, GlygenConfig.getGeneOntologies());
    }

    @Test
    void testDiseases() {
        String diseases = "in/testDiseases.rdf";
        GlygenConfig.setDiseases(diseases);
        assertEquals(diseases, GlygenConfig.getDiseases());
    }

    @Test
    void testLocations() {
        String locations = "in/testLocations.rdf";
        GlygenConfig.setLocations(locations);
        assertEquals(locations, GlygenConfig.getLocations());
    }

    @Test
    void testUpid() {
        String upid = "UP000005640";
        assertNull(config.getUpid());
        config.setUpid(upid);
        assertEquals(upid, config.getUpid());
    }

    @Test
    void testTaxId() {
        String taxId = "9606";
        assertNull(config.getTaxId());
        config.setTaxId(taxId);
        assertEquals(taxId, config.getTaxId());
    }

    @Test
    void testDbSnp() {
        String snp = "in/P05067.json";
        assertNull(config.getDbSnp());
        config.setDbSnp(snp);
        assertEquals(snp, config.getDbSnp());
    }

    @Test
    void testReactomeReactions() {
        String reactions = "in/human.txt";
        assertNull(config.getReactomeReactions());
        config.setReactomeReactions(reactions);
        assertEquals(reactions, config.getReactomeReactions());
    }


    @Test
    void testExceptionOnMissingEnzymeRdfFile() {
        GlygenConfig.setEnzyme("in/enzyme1.rdf");
        assertThrows(RuntimeException.class, GlygenConfig::createEnzymeModel);
    }

}