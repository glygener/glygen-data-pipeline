package uk.ac.ebi.uniprot.glygen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenomicLocationTest {


    @Test
    void testChromosome() {
        String chromo = "21";
        GenomicLocation genLoc = new GenomicLocation();
        assertNull(genLoc.getChromosome());
        genLoc.setChromosome(chromo);
        assertEquals(genLoc.getChromosome(), chromo);
    }


    @Test
    void testExonList() {
        GenomicLocation genLoc = new GenomicLocation();
        assertTrue(genLoc.getExonList().isEmpty());
        genLoc.addExon(new Exon());
        genLoc.addExon(new Exon());
        assertEquals(genLoc.getExonList().size(), 2);
    }


    @Test
    void testReverStrand() {
        GenomicLocation genLoc = new GenomicLocation();
        assertFalse(genLoc.isReverStrand());
        genLoc.setReverStrand(true);
        assertTrue(genLoc.isReverStrand());
    }

    @Test
    void testStart() {
        long start = 25881673l;
        GenomicLocation genLoc = new GenomicLocation();
        assertEquals(0, genLoc.getStart());
        genLoc.setStart(start);
        assertEquals(start, genLoc.getStart());
    }

    @Test
    void testEnd() {
        long end = 26170620l;
        GenomicLocation genLoc = new GenomicLocation();
        assertEquals(0, genLoc.getEnd());
        genLoc.setEnd(end);
        assertEquals(end, genLoc.getEnd());
    }
}