package uk.ac.ebi.uniprot.glygen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DbSnpVariantTest {

    @Test
    void testAccession() {
        String id = "P05067";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getAccession());
        variant.setAccession(id);
        assertEquals(id, variant.getAccession());
    }

    @Test
    void testGeneName() {
        String geneName = "GXYLT2";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getGeneName());
        variant.setGeneName(geneName);
        assertEquals(geneName, variant.getGeneName());
    }

    @Test
    void testProteinName() {
        String proteinName = "Glucoside xylosyltransferase 2";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getProteinName());
        variant.setProteinName(proteinName);
        assertEquals(proteinName, variant.getProteinName());
    }

    @Test
    void testXrefNames() {
        String xref = "1000Genomes";
        DbSnpVariant variant = new DbSnpVariant();
        assertEquals("", variant.getXrefNames());
        variant.setXrefNames(xref);
        assertEquals(xref, variant.getXrefNames());
        variant.setXrefNames("ESP");
        assertEquals(xref + ",ESP", variant.getXrefNames());
    }

    @Test
    void testXrefId() {
        String id = "rs199887707";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getXrefId());
        variant.setXrefId(id);
        assertEquals(id, variant.getXrefId());
    }

    @Test
    void testCosmicId() {
        String id = "COSM143689";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getCosmicId());
        variant.setCosmicId(id);
        assertEquals(id, variant.getCosmicId());
    }

    @Test
    void testCytogeneticBand() {
        String cb = "21q21.3";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getCytogeneticBand());
        variant.setCytogeneticBand(cb);
        assertEquals(cb, variant.getCytogeneticBand());
    }

    @Test
    void testGenomicLocationPosition() {
        String gl = "25911840";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getGenomicLocationPosition());
        variant.setGenomicLocationPosition(gl);
        assertEquals(gl, variant.getGenomicLocationPosition());
    }

    @Test
    void testWildType() {
        String wt = "V";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getWildType());
        variant.setWildType(wt);
        assertEquals(wt, variant.getWildType());
    }

    @Test
    void testAltSequence() {
        String as = "M";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getMutatedType());
        variant.setMutatedType(as);
        assertEquals(as, variant.getMutatedType());
    }

    @Test
    void testBegin() {
        String id = "604";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getBegin());
        variant.setBegin(id);
        assertEquals(id, variant.getBegin());
    }

    @Test
    void testEnd() {
        String id = "605";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getEnd());
        variant.setEnd(id);
        assertEquals(id, variant.getEnd());
    }

    @Test
    void testFrequency() {
        String freq = "0.000199681";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getFrequency());
        variant.setFrequency(freq);
        assertEquals(freq, variant.getFrequency());
    }

    @Test
    void testConsequenceType() {
        String ct = "missense";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getConsequenceType());
        variant.setConsequenceType(ct);
        assertEquals(ct, variant.getConsequenceType());
    }

    @Test
    void testPolyphenScore() {
        String ps = "0.5645";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getPolyphenScore());
        variant.setPolyphenScore(ps);
        assertEquals(ps, variant.getPolyphenScore());
    }

    @Test
    void testPolyphenPrediction() {
        String pp = "benign, probably damaging";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getPolyphenPrediction());
        variant.setPolyphenPrediction(pp);
        assertEquals(pp, variant.getPolyphenPrediction());
    }

    @Test
    void testSiftScore() {
        String ss = "0.19";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getSiftScore());
        variant.setSiftScore(ss);
        assertEquals(ss, variant.getSiftScore());
    }

    @Test
    void testSiftPrediction() {
        String sp = "tolerated";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getSiftPrediction());
        variant.setSiftPrediction(sp);
        assertEquals(sp, variant.getSiftPrediction());
    }

    @Test
    void testSomaticStatus() {
        String id = "1";
        DbSnpVariant variant = new DbSnpVariant();
        assertNull(variant.getSomaticStatus());
        variant.setSomaticStatus(id);
        assertEquals(id, variant.getSomaticStatus());
    }

    @Test
    void testAssociationName() {
        String name = "Early-Onset Familial Alzheimer Disease";
        DbSnpAssociation association = new DbSnpAssociation();
        assertTrue(association.getName().isEmpty());
        association.setName(name);
        assertEquals(name, association.getName());
    }

    @Test
    void testAssociationDesc() {
        String desc = "";
        DbSnpAssociation association = new DbSnpAssociation();
        assertTrue(association.getDescription().isEmpty());
        association.setDescription(desc);
        assertEquals(desc, association.getDescription());
    }

    @Test
    void testAssociationXrefs() {
        String xref = "MIM:104300";
        DbSnpAssociation association = new DbSnpAssociation();
        assertEquals("", association.getXrefs());
        association.setXrefs(xref);
        assertEquals(xref, association.getXrefs());
    }

    @Test
    void testAssociationEvid269() {
        String xref = "pubmed:10097173";
        DbSnpAssociation association = new DbSnpAssociation();
        assertEquals("", association.getEvidence269());
        association.setEvidence269(xref);
        assertEquals(xref, association.getEvidence269());
        association.setEvidence269("pubmed:10656250");
        assertEquals(xref + ",pubmed:10656250", association.getEvidence269());
    }

    @Test
    void testAssociationEvid313() {
        String evid = "pubmed:20298421";
        DbSnpAssociation association = new DbSnpAssociation();
        assertEquals("", association.getEvidence313());
        association.setEvidence313(evid);
        assertEquals(evid, association.getEvidence313());
        association.setEvidence313("ClinVar:RCV000019722");
        assertEquals(evid + ",ClinVar:RCV000019722", association.getEvidence313() );
    }
}
