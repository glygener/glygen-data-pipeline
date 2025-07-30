package uk.ac.ebi.uniprot.glygen.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.DbSnpAssociation;
import uk.ac.ebi.uniprot.glygen.model.DbSnpVariant;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;

/**
 * Creates dbSNP tsv for each config, if dbSNP & dbSNPOut values are set.
 * NOTE : This does not add to or modify outModel.
 */
public class DbSnpDatasetGenerator extends DefaultHandler implements DatasetGenerator {
    private static final String ASSOCIATION = "association";
    private static final String CONSEQUENCE_TYPE = "consequenceType";
    private static final String CYTOGENETIC_BAND = "cytogeneticBand";
    private static final String DB_REFERENCE = "dbReference";
    private static final String DESCRIPTION = "description";
    private static final String EVIDENCE = "evidence";
    private static final String FEATURE = "feature";
    private static final String FREQUENCY = "frequency";
    private static final String GENE_NAME = "geneName";
    private static final String GENOMIC_LOCATION = "genomicLocation";
    private static final String MUTATED_TYPE = "mutatedType";
    private static final String POPULATION_FREQUENCIES = "populationFrequencies";
    private static final String PROTEIN_NAME = "proteinName";
    private static final String SOMATIC_STATUS = "somaticStatus";
    private static final String STARTS_WITH_COSM = "COSM";
    private static final String STARTS_WITH_RS = "rs";
    private static final String DBID_SGRP = "SGRP";
    private static final String VARIANT = "Variant";
    private static final String VARIANT_PREDICTION = "variantPrediction";
    private static final String WILD_TYPE = "wildType";
    private static final String EMPTY_STRING = "";
    private static final char TAB = '\t';

    private boolean bVariant = false;
    private boolean bDesc = false;
    private int varCount = 0;
    private String sElement;
    private String eCode;

    private final GlygenConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DbSnpVariant snpVariant = new DbSnpVariant();
    private DbSnpAssociation association = null;
    private SAXParser saxParser = null;
    private Writer writer;
    private final String outputDir;

    public DbSnpDatasetGenerator(GlygenConfig config, String outputDir) {
        this.outputDir = outputDir;
        if (config.getDbSnp() == null) {
            logger.info("{} : Skipping variant info as dbSNP is not configured", config.getName());
        } else {
            try {
                saxParser = SAXParserFactory.newInstance().newSAXParser();
            } catch (Exception ex) {
                logger.error("Error creating parser ", ex);
                throw new GlyGenException("Error creating parser for {}" + config.getDbSnp());
            }
        }
        this.config = config;
    }

    @Override
    public void generateDataset() {
        if (saxParser == null) {
            return;
        }
        String gzfile = outputDir + File.separator + config.getDbSnpOut();
        if (!gzfile.endsWith(".gz")) {
            gzfile += ".gz";
        }
        try {
            //  writer = new BufferedWriter(new FileWriter(fullFilename));
            FileOutputStream output = new FileOutputStream(gzfile);
            writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8);


            writer.write(getHeader() + "\n");

            File input = new File(config.getDbSnp());
            saxParser.parse(input.getAbsoluteFile(), this);
            writer.flush();
            logger.info("Number of variants {}", varCount);
        } catch (Exception ex) {
            logger.error("Error while generating dbSNP ", ex);
            throw new GlyGenException("Error generating dbSNP Info " + ex);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String getDescription(String str) {
        return str.replace("\n", "");
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        sElement = qName;
        switch (sElement) {
            case POSITION:
                snpVariant.setBegin(attributes.getValue(POSITION));
                snpVariant.setEnd(attributes.getValue(POSITION));
                break;
            case BEGIN:
                snpVariant.setBegin(attributes.getValue(POSITION));
                break;
            case END:
                snpVariant.setEnd(attributes.getValue(POSITION));
                break;
            case DB_REFERENCE:
                handleDbReferences(attributes);
                break;
            case POPULATION_FREQUENCIES:
                handlePopulationFrequencies(attributes);
                break;
            case VARIANT_PREDICTION:
                handleVariantPrediction(attributes);
                break;
            case ASSOCIATION:
                association = new DbSnpAssociation();
                break;
            case EVIDENCE:
                eCode = attributes.getValue("code");
                break;
            case FEATURE:
                bVariant = attributes.getValue(TYPE).equals(VARIANT);
                break;
            case DESCRIPTION:
                bDesc = true;
                break;
            default:
                break;
        }
    }

    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case ASSOCIATION:
                snpVariant.addAssociation(association);
                association = null;
                break;
            case EVIDENCE:
                eCode = null;
                break;
            case FEATURE:
                try {
                    if (bVariant) {
                        printVariant(writer);
                    }
                    bVariant = false;
                } catch (Exception ex) {
                    throw new GlyGenException("Error updating dbSNP Info " + ex);
                }
                break;
            case DESCRIPTION:
                bDesc = false;
                break;
            default:
                break;
        }
    }

    public void characters(char[] ch, int start, int length) {
        String value = new String(ch, start, length);
        switch (sElement) {
            case NAME:
                if (association != null) association.setName(value);
                break;

            case ACCESSION:
                snpVariant = new DbSnpVariant();
                snpVariant.setAccession(value);
                break;

            case GENE_NAME:
                snpVariant.setGeneName(value);
                break;

            case PROTEIN_NAME:
                snpVariant.setProteinName(value);
                break;

            case CYTOGENETIC_BAND:
                snpVariant.setCytogeneticBand(value);
                break;

            case GENOMIC_LOCATION:
                handleGenomicLocation(value);
                break;

            case SOURCE:
                if (bDesc) snpVariant.addDescriptionSource(value);
                break;

            case VALUE:
                snpVariant.addDescriptionValue(getDescription(value));
                break;

            case CONSEQUENCE_TYPE:
                snpVariant.setConsequenceType(value);
                break;

            case SOMATIC_STATUS:
                snpVariant.setSomaticStatus(value);
                break;

            case WILD_TYPE:
                snpVariant.setWildType(value);
                break;

            case MUTATED_TYPE:
                snpVariant.setMutatedType(value);
                break;

            case DESCRIPTION:
                association.setDescription(value);
                break;

            default:
                break;
        }
    }

    private void handlePopulationFrequencies(Attributes attributes) {
        if (attributes.getValue(SOURCE).equals("1000Genomes")) {
            snpVariant.setFrequency(attributes.getValue(FREQUENCY));
        }
    }

    private void handleVariantPrediction(Attributes attributes) {
        if (attributes.getValue("predAlgorithmNameType").equals("PolyPhen")) {
            snpVariant.setPolyphenScore(attributes.getValue("score"));
            snpVariant.setPolyphenPrediction(attributes.getValue("predictionValType"));
        } else if (attributes.getValue("predAlgorithmNameType").equals("SIFT")) {
            snpVariant.setSiftScore(attributes.getValue("score"));
            snpVariant.setSiftPrediction(attributes.getValue("predictionValType"));
        }
    }

    private void handleGenomicLocation(String value) {
        if (value.startsWith("NC")) {
            snpVariant.setGenomicLocationPosition(value.substring(value.lastIndexOf('.') + 1, value.length() - 3));
            snpVariant.setRefAllele(value.substring(value.length() - 1));
        } else if (value.length() > 2 && (value.charAt(1) == ':' || value.charAt(2) == ':')) {
            snpVariant.setGenomicLocationPosition(value.substring(0, value.length() - 3));
            snpVariant.setRefAllele(value.substring(value.length() - 1));
        } else if (value.length() == 1) {
            if (!value.equals(">"))
                snpVariant.setAltAllele(value);
//        } else {
//            logger.debug("Non NC genomic location --> {} ", value);
        }
    }

    private void handleDbReferences(Attributes attributes) {
        String id = attributes.getValue(ID);
        String type = attributes.getValue(TYPE);
        if (eCode != null) {
            handleEvidenceDbReference(eCode, type + CHAR_COLON + id);
        } else if (association != null) {
            association.setXrefs(type + CHAR_COLON + id);
        } else {
            if (id.startsWith(STARTS_WITH_RS)) {
                if (snpVariant.getXrefId() == null) {
                    snpVariant.setXrefId(id);
                }
                if (snpVariant.getXrefId().equals(id)) {
                    snpVariant.setXrefNames(type);
                } else if (type != null) {
                    logger.warn(" check multiple ids {}, pos {} - {} : {} :: {} : {}", snpVariant.getAccession(),
                            snpVariant.getBegin(), snpVariant.getXrefId(), snpVariant.getXrefNames(), id, type);
                }
            } else if (id.startsWith(STARTS_WITH_COSM)) {
                snpVariant.setCosmicId(type);
            } else if (type.equals(DBID_SGRP)) {
                if (snpVariant.getXrefId() == null) {
                    snpVariant.setXrefId(id);
                    snpVariant.setXrefNames(type);
                } else {
                    snpVariant.setXrefId(snpVariant.getXrefId() + "," + id);
                }
            }
        }
    }

    private void handleEvidenceDbReference(String eCode, String value) {
        switch (eCode) {
            case "ECO:0000269":
                if (association == null) {
                    snpVariant.setEvidence269(value);
                } else {
                    association.setEvidence269(value);
                }
                break;

            case "ECO:0000313":
                if (association == null) {
                    snpVariant.setEvidence313(value);
                } else {
                    association.setEvidence313(value);
                }
                break;

            default:
                logger.warn(" WARNING: Unhandled eco code: {}", eCode);
                break;
        }
    }

    private void printVariant(Writer writer) throws IOException {
        if (snpVariant != null && snpVariant.getXrefId() != null && snpVariant.getGenomicLocationPosition() != null) {
            ++varCount;
            if (snpVariant.getAssociations().isEmpty()) {
                writer.write(getDbSnpLine(snpVariant, new DbSnpAssociation()) + "\n");
            } else {
                for (DbSnpAssociation association : snpVariant.getAssociations()) {
                    writer.write(getDbSnpLine(snpVariant, association) + "\n");
                }
            }
        }
        snpVariant = new DbSnpVariant(snpVariant);
    }

    /**
     * fields_in_csv       => fields_in_api
     * 1	uniprotkb_accession => accession
     * 2	gene_name           => geneName
     * 3	protein_name        => proteinName
     * 4	data_source	        => xref:name
     * 5	dbsnp_id            => xref: id
     * 6	cosmic_id           => xref: id
     * 7	variant_description	=> variant description
     * 8	variant_EC:0000269	=> dbReference: type & id of <evidence code="ECO:0000269">
     * 9	variant_EC:0000313	=> dbReference: type & id of <evidence code="ECO:0000313">
     * 10	cytogenic_band	    => cytogeneticBand
     * 11	chromosome_id	    => genomicLocation
     * 12	position	        => genomicLocation
     * 13	ref_allele	        => genomicLocation
     * 14	alt_allele	        => genomicLocation
     * 15	ref_aa	            => wildType
     * 16	alt_aa	            => mutatedType
     * 17	begin_aa_pos	    => begin (or position)
     * 18	end_aa_pos	        => end (or position)
     * 19	frequency	        => frequency
     * 20	mutation_type	    => consequenceType
     * 21	polyphen_score	    => polyphenScore
     * 22	polyphen_prediction	=> polyphenPrediction
     * 23	sift_score	        => siftScore
     * 24	sift_prediction	    => siftPrediction
     * 25	somatic_status	    => somaticStatus
     * 26	disease	            => association:name
     * 27	disease_description	=> association:description
     * 28	disease_xrefs	    => association:xrefs:name & id
     * 29	disease_EC:0000269	=> association:: dbReference: type & id of <evidence code="ECO:0000269">
     * 30	disease_EC:0000313	=> association:: dbReference: type & id of <evidence code="ECO:0000313">
     */

    private String getHeader() {
        return "uniprotkb_accession\t" +
                "gene_name\t" +
                "protein_name\t" +
                "data_source\t" +
                "dbsnp_id\t" +
                "cosmic_id\t" +
                "description\t" +
                "evidence_ECO:0000269\t" +
                "evidence_ECO:0000313\t" +
                "cytogenic_band\t" +
                "chromosome_id\t" +
                "position\t" +
                "ref_allele\t" +
                "alt_allele\t" +
                "ref_aa\t" +
                "alt_aa\t" +
                "begin_aa_pos\t" +
                "end_aa_pos\t" +
                "frequency\t" +
                "mutation_type\t" +
                "polyphen_score\t" +
                "polyphen_prediction\t" +
                "sift_score\t" +
                "sift_prediction\t" +
                "somatic_status\t" +
                "disease\t" +
                "disease_description\t" +
                "disease_xrefs\t" +
                "disease_evidence_ECO:0000269\t" +
                "disease_evidence_ECO:0000313\t";
    }

    private String getNonNullValue(String value) {
        return value == null ? EMPTY_STRING : value;
    }

    private void appendTabN(StringBuilder sb, String val) {
        sb.append(getNonNullValue(val));
        sb.append(TAB);
    }

    private void appendTab(StringBuilder sb, String val) {
        sb.append(val);
        sb.append(TAB);
    }

    private String getDbSnpLine(DbSnpVariant snpVariant, DbSnpAssociation association) {
        StringBuilder sb = new StringBuilder();

        appendTab(sb, snpVariant.getAccession());
        appendTab(sb, snpVariant.getGeneName());
        appendTab(sb, snpVariant.getProteinName());
        appendTab(sb, snpVariant.getXrefNames());
        appendTab(sb, snpVariant.getXrefId());
        appendTabN(sb, snpVariant.getCosmicId());   //6
        appendTabN(sb, snpVariant.getDescription());
        appendTab(sb, snpVariant.getEvidence269());
        appendTab(sb, snpVariant.getEvidence313());
        appendTab(sb, snpVariant.getCytogeneticBand());

        String ch = "";
        if (snpVariant.getCytogeneticBand() != null && !snpVariant.getCytogeneticBand().isEmpty()) {
            ch += snpVariant.getCytogeneticBand().charAt(0);
            if (snpVariant.getCytogeneticBand().length() > 1) {
                char c = snpVariant.getCytogeneticBand().charAt(1);
                if (c >= '0' && c <= '9') {
                    ch += c;
                }
            }
        }

        appendTab(sb, ch);   // chromosome_id
        appendTab(sb, snpVariant.getGenomicLocationPosition());    // position
        appendTab(sb, snpVariant.getRefAllele());
        appendTab(sb, snpVariant.getAltAllele());
        appendTab(sb, snpVariant.getWildType());    //15
        appendTab(sb, snpVariant.getMutatedType());
        appendTab(sb, snpVariant.getBegin());
        appendTab(sb, snpVariant.getEnd());
        appendTabN(sb, snpVariant.getFrequency());
        appendTabN(sb, snpVariant.getConsequenceType());
        appendTabN(sb, snpVariant.getPolyphenScore());
        appendTabN(sb, snpVariant.getPolyphenPrediction());
        appendTabN(sb, snpVariant.getSiftScore());
        appendTabN(sb, snpVariant.getSiftPrediction());
        appendTab(sb, snpVariant.getSomaticStatus());

        // disease info
        appendTab(sb, association.getName());
        appendTab(sb, association.getDescription());
        appendTab(sb, association.getXrefs());
        appendTab(sb, association.getEvidence269());
        appendTab(sb, association.getEvidence313());

        return sb.toString();
    }
}
