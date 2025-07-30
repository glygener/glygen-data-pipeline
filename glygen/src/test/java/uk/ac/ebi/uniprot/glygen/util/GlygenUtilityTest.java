package uk.ac.ebi.uniprot.glygen.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlygenUtilityTest {
    // tr sequence name
    private final String trSeqName = ">tr|A0A075B6F4|A0A075B6F4_HUMAN T-cell receptor beta variable 21/OR9-2 (pseudogene) (Fragment) OS=Homo sapiens GN=TRBV21OR9-2 PE=4 SV=1";
    // sp isoform sequence name
    private final String spIsoSeqName = ">sp|A0FGR8-6|ESYT2_HUMAN Isoform of A0FGR8, Isoform 6 of Extended synaptotagmin-2 OS=Homo sapiens GN=ESYT2";

    @Test
    void getUniprotIdFromValidSequenceName() {
        assertEquals("A0A075B6F4", GlygenUtility.getUniprotIdFromSequenceName(trSeqName));
        assertEquals("A0A075B6F4", GlygenUtility.getUniprotIdFromSequenceName(trSeqName.substring(1)));
        assertEquals("A0FGR8-6", GlygenUtility.getUniprotIdFromSequenceName(spIsoSeqName));

        assertNull(GlygenUtility.getUniprotIdFromSequenceName("invalid"));
    }

    @Test
    void getCanonicalFromSequenceName() {
        assertEquals("A0FGR8", GlygenUtility.getCanonicalFromSequenceName(spIsoSeqName));
        assertNull(GlygenUtility.getCanonicalFromSequenceName(trSeqName));

        String trIsoSeqName = ">tr|A0A1W2PRR4|A0A1W2PRR4_HUMAN Isoform of R4GMW8, BIVM-ERCC5 readthrough (Fragment) OS=Homo sapiens GN=BIVM-ERCC5 PE=4 SV=1";
        assertEquals("R4GMW8", GlygenUtility.getCanonicalFromSequenceName(trIsoSeqName));
    }
    
    @Test
    void testfileType() {
        String filename ="/Users/jluo/projects/glygen/temp/in/UP000002195_44689_uniprot_proteome.rdf.gz";
        String newFile ="/Users/jluo/projects/glygen/temp/in/UP000002195_44689_uniprot_proteome.rdf";
        boolean result =FileUtils.decompressGZFile(filename, newFile);
        System.out.println(result);
    }
    
    @Test
    void test() {
        String s ="X8AMQ7                        A0A1A2QRE0";
        String [] tokens = s.split("\\s+");
        for(String token:tokens) {
            System.out.println(token);
        }
    }

}