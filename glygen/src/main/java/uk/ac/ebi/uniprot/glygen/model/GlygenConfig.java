package uk.ac.ebi.uniprot.glygen.model;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.CHAR_DASH;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.CHAR_PLUS;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.ISOFORM_PREFIX;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN_PREFIX;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.getAbsFileName;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.createModelFromRdfFile;

public class GlygenConfig {
    private static final String OUT = "out";
    private static final String XXXX = "xxxx";
    private static String reactomeNeo4jInfo;
    private static String enzyme;
    private static String tissues;
    private static String keywords;
    private static String geneOntologies;
    private static String diseases;
    private static String databases;
    private static String locations;
    private static String rhea;
    private static String canonicalMapOut;
    private static String dbXrefMapOut;
    private static Model enzModel;
    private static Map<String, String> intActIdMap;
    private static String rdfOutput;
    private static String statisticsFile;
    private static String dbSnpOut;

    private String name;
    private String dbDir;
    private String canonical;
    private String isoform;
    private String geneCoordinate;
    private String ensemblCds;
    private String ensemblPeptide;
    private String taxId;
    private String speciesTaxId;
    private String upid;
    private String dbSnp;
    private String reactomeReactions;
    private String geneInfo;
    private String transcriptDb;
    private String source="Ensembl";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbDir() {
        return dbDir;
    }

    public String getDbDirOut() {
        return dbDir + OUT;
    }

    public void setDbDir(String dbDir) {
        this.dbDir = dbDir;
    }

    public String getCanonical() {
        return  canonical;
    }

    public void setCanonical(String canonical) {
        this.canonical = canonical;
    }

    public String getIsoform() {
        return  isoform;
    }

    public void setIsoform(String isoform) {
        this.isoform = isoform;
    }

    public String getGeneCoordinate() {
        return  geneCoordinate;
    }

    public void setGeneCoordinate(String geneCoordinate) {
        this.geneCoordinate = geneCoordinate;
    }

    public String getEnsemblCds() {
        return  ensemblCds;
    }

    public void setEnsemblCds(String ensemblCds) {
        this.ensemblCds = ensemblCds;
    }

    public String getEnsemblPeptide() {
        return  ensemblPeptide;
    }

    public void setEnsemblPeptide(String ensemblPep) {
        this.ensemblPeptide = ensemblPep;
    }

    public String getRdfOutput() {
        return  rdfOutput.replace(XXXX, this.name);
    }

    public static void setRdfOutput(String rdfOut) {
        rdfOutput = rdfOut;
    }

    public static String getTissues() {
        return  tissues;
    }

    public static void setTissues(String val) {
        tissues = val;
    }

    public static String getKeywords() {
        return  keywords;
    }

    public static void setKeywords(String val) {
        keywords = val;
    }

    public static String getGeneOntologies() {
        return  geneOntologies;
    }

    public static void setGeneOntologies(String val) {
        geneOntologies = val;
    }

    public static String getEnzyme() {
        return  enzyme;
    }

    public static Model getEnzymeModel() {
        return enzModel;
    }

    public static void setEnzyme(String val) {
        enzyme = val;
    }

    public static void createEnzymeModel() {
        enzModel = createModelFromRdfFile(getEnzyme());
    }

    public static String getDatabases() {
        return databases;
    }

    public static void setDatabases(String val) {
        databases = val;
    }

    public static String getDiseases() {
        return  diseases;
    }

    public static void setDiseases(String val) {
        diseases = val;
    }

    public static String getLocations() {
        return  locations;
    }

    public static void setLocations(String val) {
        locations = val;
    }

    public static String getCanonicalMapOut() {
        return canonicalMapOut;
    }

    public static void setCanonicalMapOut(String val) {
        canonicalMapOut = val;
    }

    public static String getDbXrefMapOut() {
        return dbXrefMapOut;
    }

    public static void setDbXrefMapOut(String val) {
        dbXrefMapOut = val;
    }

    public String getUpid() {
        return upid;
    }

    public void setUpid(String upid) {
        this.upid = upid;
    }

    public String getStatisticsFile() {
        return statisticsFile.replace(XXXX, this.name);
    }

    public static void setStatisticsFile(String statsFile) {
        statisticsFile = statsFile;
    }

    public static String getReactomeNeo4jInfo() {
        return reactomeNeo4jInfo;
    }

    public static void setReactomeNeo4jInfo(String info) {
        reactomeNeo4jInfo = info;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getSpeciesTaxId() {
        return speciesTaxId;
    }

    public void setSpeciesTaxId(String taxId) {
        this.speciesTaxId = taxId;
    }

    public String getDbSnp() {
        return dbSnp;
    }

    public void setDbSnp(String dbSnp) {
        this.dbSnp = dbSnp;
    }

    public String getDbSnpOut() {
        return dbSnpOut.replace(XXXX, this.name);
    }

    public static void setDbSnpOut(String dbOut) {
        dbSnpOut = dbOut;
    }

    public String getReactomeReactions() {
        return reactomeReactions;
    }

    public void setReactomeReactions(String reactomeReactions) {
        this.reactomeReactions = reactomeReactions;
    }

    public String getRhea() {
        return rhea;
    }

    public static void setRhea(String val) {
        rhea = val;
    }

    public String getGeneInfo() {
        return geneInfo;
    }

    public void setGeneInfo(String geneInfo) {
        this.geneInfo = geneInfo;
    }

    public String getTranscriptDb() {
        return transcriptDb;
    }

    public void setTranscriptDb(String transcriptDb) {
        this.transcriptDb = transcriptDb;
    }

    
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static void setIntAct(String intAct) {
        try (BufferedReader reader = new BufferedReader(new FileReader(getAbsFileName(intAct)))) {
            intActIdMap = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] sr = line.split(" ");
                String key = sr[0].substring(sr[0].indexOf(CHAR_DASH)+1);
                String val = sr[1];
                if (val.indexOf(CHAR_PLUS) > -1) {
                    val = val.substring(0, val.indexOf(CHAR_PLUS));
                }
                intActIdMap.put(key, val);
            }
        } catch (IOException e) {
            throw new GlyGenException("Error creating IntAct id mapping using " + intAct, e);
        }
    }

    public static String getSameAsAttributeForIntActId(String id) {
        String key = id.substring(id.indexOf(CHAR_DASH) + 1);
        if (!intActIdMap.containsKey(key)) {
            return id;
        }

        if (intActIdMap.get(key).contains("-PRO_")) {
            return PROTEIN_PREFIX + intActIdMap.get(key);
        }

        if (intActIdMap.get(key).indexOf(CHAR_DASH) > -1) {
            return ISOFORM_PREFIX + intActIdMap.get(key);
        }

        return PROTEIN_PREFIX + intActIdMap.get(key);
    }
}