package uk.ac.ebi.uniprot.glygen.model;

/**
 * Class to denote exonMapType in uniprot_genome_coordinate.xsd
 */

public class Exon {
    private String exonId;
    private long genomicLocationBegin;
    private long genomicLocationEnd = -1;

    public void setExonId(String exonId) {
        this.exonId = exonId;
    }

    public String getExonId() {
        return exonId;
    }

    public long getGenomicLocationBegin() {
        return genomicLocationBegin;
    }

    public void setGenomicLocationBegin(long genomicLocationBegin) {
        this.genomicLocationBegin = genomicLocationBegin;
    }

    public long getGenomicLocationEnd() {
        return genomicLocationEnd;
    }

    public void setGenomicLocationEnd(long genomicLocationEnd) {
        this.genomicLocationEnd = genomicLocationEnd;
    }
}
