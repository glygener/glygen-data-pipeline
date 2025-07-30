package uk.ac.ebi.uniprot.glygen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExonTest {

    @Test
    void testExonId() {
        String id = "ENSE00001909719";
        Exon exon = new Exon();
        assertNull(exon.getExonId());
        exon.setExonId(id);
        assertEquals(id, exon.getExonId());
    }


    @Test
    void testGenomicLocationBegin() {
        long locBegin = 26170564L;
        Exon exon = new Exon();
        assertEquals(0, exon.getGenomicLocationBegin());
        exon.setGenomicLocationBegin(locBegin);
        assertEquals(locBegin, exon.getGenomicLocationBegin());
    }


    @Test
    void testGenomicLocationEnd() {
        long locEnd = 26170620L;
        Exon exon = new Exon();
        assertEquals(-1, exon.getGenomicLocationEnd());
        exon.setGenomicLocationEnd(locEnd);
        assertEquals(locEnd, exon.getGenomicLocationEnd());
    }

}