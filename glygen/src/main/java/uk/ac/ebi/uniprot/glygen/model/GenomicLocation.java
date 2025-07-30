package uk.ac.ebi.uniprot.glygen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to capture required data from genomicLocationType in uniprot_genome_coordinate.xsd
 */
public class GenomicLocation {
    private long start;
    private long end;
    private String chromosome;
    private boolean reverStrand;
    private List<Exon> exonList;

    public GenomicLocation() {
        this.exonList = new ArrayList<>();
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public boolean isReverStrand() {
        return reverStrand;
    }

    public void setReverStrand(boolean reverStrand) {
        this.reverStrand = reverStrand;
    }

    public List<Exon> getExonList() {
        return exonList;
    }

    public void addExon(Exon exon) {
        this.exonList.add(exon);
    }
}
