package uk.ac.ebi.uniprot.glygen.model;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class DbSnpVariant {
    private final static DecimalFormat freqFormat =
            new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private String accession;
    private String geneName;
    private String proteinName;
    private String xrefNames = "";
    private String xrefId;
    private String cosmicId;
    private String cytogeneticBand;
    private String genomicLocationPosition;
    private String refAllele;
    private String altAllele;
    private String wildType;
    private String mutatedType; //mutatedType in xml, alt_aa in tsv
    private String description = "";
    private String begin;
    private String end;
    private String frequency;
    private String consequenceType; // consequenceType in xml, mutation_type in tsv
    private String polyphenScore;
    private String polyphenPrediction;
    private String siftScore;
    private String siftPrediction;
    private String somaticStatus;
    private final ArrayList<DbSnpAssociation> associations = new ArrayList<>();
    private String evidence269 = ""; // <sourceName:ID>,<sourceName:ID>
    private String evidence313 = ""; // <sourceName:ID>,<sourceName:ID>

    static {
        freqFormat.setMaximumFractionDigits(340);
    }

    public DbSnpVariant() {}

    public DbSnpVariant(DbSnpVariant variant) {
        if (variant != null) {
            this.accession = variant.getAccession();
            this.geneName = variant.getGeneName();
            this.proteinName = variant.getProteinName();
        }
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getProteinName() {
        return proteinName;
    }

    public void setProteinName(String proteinName) {
        this.proteinName = proteinName;
    }

    public String getXrefNames() {
        return xrefNames;
    }

    public void setXrefNames(String name) {
        if (!xrefNames.isEmpty())
            xrefNames += ',';
        xrefNames += name;
    }

    public String getXrefId() {
        return xrefId;
    }

    public void setXrefId(String xrefId) {
        this.xrefId = xrefId;
    }

    public String getCosmicId() {
        return cosmicId;
    }

    public void setCosmicId(String cosId) {
        this.cosmicId = cosId;
    }

    public String getCytogeneticBand() {
        return cytogeneticBand;
    }

    public void setCytogeneticBand(String cytogeneticBand) {
        this.cytogeneticBand = cytogeneticBand;
    }

    public String getGenomicLocationPosition() {
        return genomicLocationPosition;
    }

    public void setGenomicLocationPosition(String pos) {
        this.genomicLocationPosition = pos;
    }

    public String getWildType() {
        return wildType;
    }

    public void setWildType(String wildType) {
        this.wildType = wildType;
    }

    public String getMutatedType() {
        return mutatedType;
    }

    public void setMutatedType(String mutatedType) {
        this.mutatedType = mutatedType;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        try {
            double d = Double.parseDouble(frequency);
            this.frequency = freqFormat.format(d);
        } catch (NumberFormatException ne) {
            throw new GlyGenException(ne);
        }
    }

    public String getConsequenceType() {
        return consequenceType;
    }

    public void setConsequenceType(String consequenceType) {
        this.consequenceType = consequenceType;
    }

    public String getPolyphenScore() {
        return polyphenScore;
    }

    public void setPolyphenScore(String polyphenScore) {
        this.polyphenScore = polyphenScore;
    }

    public String getPolyphenPrediction() {
        return polyphenPrediction;
    }

    public void setPolyphenPrediction(String polyphenPrediction) {
        this.polyphenPrediction = polyphenPrediction;
    }

    public String getSiftScore() {
        return siftScore;
    }

    public void setSiftScore(String siftScore) {
        this.siftScore = siftScore;
    }

    public String getSiftPrediction() {
        return siftPrediction;
    }

    public void setSiftPrediction(String siftPrediction) {
        this.siftPrediction = siftPrediction;
    }

    public String getSomaticStatus() {
        return somaticStatus;
    }

    public void setSomaticStatus(String somaticStatus) {
        this.somaticStatus = somaticStatus.equals("false") ? "0":"1";
    }

    public String getDescription() {
        return description;
    }

    public void addDescriptionSource(String source) {
        description = "[" + source + "]: " + description;
    }

    public void addDescriptionValue(String value) {
        if (!description.isEmpty()) {
            description = ", " + description;
        }
        this.description = value + description;
    }

    public String getEvidence269() {
        return evidence269;
    }

    public void setEvidence269(String val) {
        if (!evidence269.isEmpty()) {
            evidence269 += ",";
        }
        evidence269 += val;
    }

    public String getEvidence313() {
        return evidence313;
    }

    public void setEvidence313(String val) {
        if (!evidence313.isEmpty()) {
            evidence313 += ",";
        }
        evidence313 += val;
    }

    public ArrayList<DbSnpAssociation> getAssociations() {
        return associations;
    }

    public void addAssociation(DbSnpAssociation association) {
        associations.add(association);
    }

    public String getRefAllele() {
        return refAllele;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    public String getAltAllele() {
        return altAllele;
    }

    public void setAltAllele(String altAllele) {
        this.altAllele = altAllele;
    }
}
