package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.Exon;
import uk.ac.ebi.uniprot.glygen.model.GenomicLocation;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;

/**
 * Class to add genomic coordinates for each isoform in glygen dataset
 */
public class GenomicCoordinateAppender implements DataAppender {
    private static final String REF_SEQ = "RefSeq";
    private final Logger logger = LoggerFactory.getLogger(GenomicCoordinateAppender.class);
    private final Set<String> exonRangeSet = new HashSet<>();
    private Model outModel;
    private GlygenDataset dataset;
    private JsonParser jsonParser;
    private String isoId = null;
    private GenomicLocation gnLocation = null;
    private Map<String, GenomicLocation> genomicLocationMap;
    private int added;
    private int notAdded;
    private GlygenConfig config;

    public static void main(String[] args) throws Exception {
        File file = new File("C:\\Users\\pvasudev\\IdeaProjects\\unp.fw.glygen\\in\\UP000005640_9606.coordinates.json");
        JsonParser jsonParser = new JsonFactory().createParser(file);
        Set<String> set = new HashSet<>();
        while (jsonParser.nextToken() != null) {
            if (jsonParser.getCurrentName() != null && jsonParser.getCurrentName().equals(ACCESSION)) {
                if (jsonParser.currentToken().equals(JsonToken.FIELD_NAME)) {
                    jsonParser.nextToken();
                }
                set.add(jsonParser.getText());
            }
        }
        System.out.println("Accession count " + set.size());
        System.out.println("==========" + set);
    }

    public GenomicCoordinateAppender(GlygenConfig config) {
        if (config.getGeneCoordinate() == null) {
            logger.info("{}: Skipping GeneCoordinate data as geneCoordinate is not configured", config.getName());
        } else {
            this.config = config;
            File file = new File(getAbsFileName(config.getGeneCoordinate()));
            try {
                jsonParser = new JsonFactory().createParser(file);
            } catch (IOException ex) {
                logger.error("Error reading input file - {}", file.getPath(), ex);
                throw new GlyGenException("Error reading input file - " + file.getPath());
            }
        }
    }

    @Override
    public void appendData(GlygenDataset dataSet) {
        if (jsonParser == null) {
            return;
        }
        dataset = dataSet;
        outModel = dataSet.getOutModel();
        added = 0;
        notAdded = 0;

        try {
            parseJson();
        } catch (IOException ex) {
            logger.error("Error while appending Genomic Coordinates ", ex);
            throw new GlyGenException("Error appending Gene Coordinates Info ", ex);
        }
        logger.info("ENSTranscripts added: {} not added: {}", added, notAdded);
    }

    private void parseJson() throws IOException {

        while (jsonParser.nextToken() != null) {
            String name = jsonParser.getCurrentName();
            if (ACCESSION.equals(name)) {
                handleAccession();

            } else if (EXON.equals(name)) {
                handleExon();

            } else if (CHROMOSOME.equals(name)) {
                jsonParser.nextToken();
                gnLocation.setChromosome(jsonParser.getValueAsString());

            } else if (START.equals(name)) {
                jsonParser.nextToken();
                gnLocation.setStart(jsonParser.getLongValue());

            } else if (END.equals(name)) {
                jsonParser.nextToken();
                gnLocation.setEnd(jsonParser.getLongValue());

            } else if (REVERSE_STRAND.equals(name)) {
                jsonParser.nextToken();
                gnLocation.setReverStrand(jsonParser.getBooleanValue());

            } else {
                if ((config.getSource() != null) && (config.getSource().equals(REF_SEQ))) {
                    handleRefSeqProtein(name);
                } else {
                    handleEnsemblTranscript(name);
                }
            }
        }
        if (isoId != null) {
            if ((config.getSource() != null) && (config.getSource().equals(REF_SEQ))) {
                updateDataset(REFSEQ_PROTEIN_PREFIX);
            }else {
                updateDataset(ENS_TRANSCRIPT_PREFIX);
            }
        }
    }

    private void handleRefSeqProtein(String name) throws IOException {
        if (name != null && Arrays.binarySearch(SKIP_NAMES, name) > -1) {
            jsonParser.skipChildren();

        } else if (REFSEQ_PROTEIN_ID.equals(name)) {
            if (jsonParser.currentToken().equals(JsonToken.FIELD_NAME)) {
                jsonParser.nextToken(); // VALUE_STRING
            }

            // transcriptId : gnlocation
            genomicLocationMap.put(jsonParser.getValueAsString(), gnLocation);
            gnLocation = new GenomicLocation();
            jsonParser.nextToken(); // END_OBJECT
            jsonParser.nextToken(); // END_ARRAY
            jsonParser.nextToken(); // END_OBJECT
        } else {
            jsonParser.nextToken();
        }
    }

    private void handleEnsemblTranscript(String name) throws IOException {
        if (name != null && Arrays.binarySearch(SKIP_NAMES, name) > -1) {
            jsonParser.skipChildren();

        } else if (ENSEMBL_TRANSCRIPT_ID.equals(name)) {
            if (jsonParser.currentToken().equals(JsonToken.FIELD_NAME)) {
                jsonParser.nextToken(); // VALUE_STRING
            }

            // transcriptId : gnlocation
            genomicLocationMap.put(jsonParser.getValueAsString(), gnLocation);
            gnLocation = new GenomicLocation();
            jsonParser.nextToken(); // ENSP key
            jsonParser.nextToken(); // ENSP id
            jsonParser.nextToken(); // END_OBJECT
            jsonParser.nextToken(); // END_ARRAY
            jsonParser.nextToken(); // END_OBJECT
        } else {
            jsonParser.nextToken();
        }
    }

    private void updateDataset(String rdfPrefix) {
        // as of 2024_03 ena, refseq & ensembl genomic co-ordinates are available, but
        // only ena/refseq have transcriptId version number
        genomicLocationMap.keySet().forEach(transId -> {
            // for ensembl species 'transId' in json doesn't have version. ena/refseq
            // species have version
            GenomicLocation genLoc = genomicLocationMap.get(transId);
            String resTransId = transId.contains(".") ? transId : dataset.getTranscriptId(transId);
            Resource transcript = outModel.createResource(rdfPrefix + resTransId);
            if (transcript.hasProperty(RDF.type)) {
                if (genLoc.getChromosome() == null) {
                    logger.debug("Chromosome is null for {} : {}", resTransId, isoId);
                } else {
                    transcript.addProperty(outModel.createProperty(GLY_CHROMOSOME),
                            outModel.createLiteral(genLoc.getChromosome()));
                }
                transcript.addProperty(outModel.createProperty(GLY_REVERSE_STRAND),
                        outModel.createTypedLiteral(genLoc.isReverStrand()));

                addTranscriptRange(genLoc, transcript);
                addExons(genLoc, transcript, resTransId);
                added++;
            } else {
                notAdded++;
            }
        });
    }
   
    private void addTranscriptRange(GenomicLocation genLoc, Resource transcript) {
        Resource transRange = outModel.createResource(getUri(RANGE_PREFIX));
        transRange.addProperty(RDF.type, outModel.createResource(FALDO_TYPE_REGION));
        transRange.addProperty(outModel.createProperty(FALDO_BEGIN), dataset.createPosition(genLoc.getStart()));
        transRange.addProperty(outModel.createProperty(FALDO_END), dataset.createPosition(genLoc.getEnd()));

        transcript.addProperty(outModel.createProperty(GLY_TRANSCRIPT_RANGE), transRange);
    }

    private void addExons(GenomicLocation genLoc, Resource transcript, String transId) {
        for (Exon exon : genLoc.getExonList()) {
            Resource location = dataset.createPosition(exon.getGenomicLocationBegin());

            String rangeId = transId + URI_CHAR_SEPARATOR + exon.getExonId();
            if (!exonRangeSet.contains(rangeId)) {
                Resource exonRange = outModel.createResource(getUri(RANGE_PREFIX));
                exonRange.addProperty(RDF.type, outModel.createResource(FALDO_TYPE_REGION));
                exonRange.addProperty(outModel.createProperty(FALDO_BEGIN), location);
                if (exon.getGenomicLocationEnd() == -1) {
                    exonRange.addProperty(outModel.createProperty(FALDO_END), location);
                } else {
                    exonRange.addProperty(outModel.createProperty(FALDO_END),
                            dataset.createPosition(exon.getGenomicLocationEnd()));
                }

                transcript.addProperty(outModel.createProperty(GLY_EXON_RANGE), exonRange);
                exonRangeSet.add(rangeId);
            }
        }
    }

    private void handleAccession() throws IOException {
        if (isoId != null) {
            if ((config.getSource() != null) && (config.getSource().equals(REF_SEQ))) {
                updateDataset(REFSEQ_PROTEIN_PREFIX);
            }else {
                updateDataset(ENS_TRANSCRIPT_PREFIX);
            }
        }

        gnLocation = new GenomicLocation();
        genomicLocationMap = new HashMap<>();
        if (jsonParser.currentToken().equals(JsonToken.FIELD_NAME)) {
            jsonParser.nextToken();
        }
        isoId = jsonParser.getText();
    }

    private void handleExon() throws IOException {
        // START_ARRAY
        jsonParser.nextToken();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            Exon exon = new Exon();

            // proteinLocation
            handleProteinLocation();

            // genomeLocation
            handleGenomeLocation(exon);

            while (!ID.equals(jsonParser.getCurrentName())) {
                jsonParser.nextToken();
            }
            jsonParser.nextToken();
            exon.setExonId(jsonParser.getValueAsString());
            jsonParser.nextToken();
            gnLocation.addExon(exon);
        }
    }

    private void handleProteinLocation() throws IOException {
        while (!PROTEIN_LOCATION.equals(jsonParser.getCurrentName())) {
            jsonParser.nextToken();
        }
        while (!(BEGIN.equals(jsonParser.getCurrentName()) || POSITION.equals(jsonParser.getCurrentName()))) {
            jsonParser.nextToken();
        }
        if (BEGIN.equals(jsonParser.getCurrentName())) {
            readPosition(jsonParser);
        } else {
            jsonParser.nextToken();
            jsonParser.nextToken();
        }
        readPosition(jsonParser);
    }

    private void handleGenomeLocation(Exon exon) throws IOException {
        while (!GENOME_LOCATION.equals(jsonParser.getCurrentName())) {
            jsonParser.nextToken();
        }
        while (!(BEGIN.equals(jsonParser.getCurrentName()) || POSITION.equals(jsonParser.getCurrentName()))) {
            jsonParser.nextToken();
        }
        if (BEGIN.equals(jsonParser.getCurrentName())) {
            exon.setGenomicLocationBegin(readPosition(jsonParser));
            exon.setGenomicLocationEnd(readPosition(jsonParser));
        } else {
            jsonParser.nextToken();
            jsonParser.nextToken();
            exon.setGenomicLocationBegin(readPosition(jsonParser));
        }
    }

    private long readPosition(JsonParser parser) throws IOException {
        long val;
        while (!POSITION.equals(parser.getCurrentName())) {
            parser.nextToken();
        }
        parser.nextToken();
        val = parser.getLongValue();
        parser.nextToken();
        return val;
    }

    private static final String[] SKIP_NAMES = { "alternativeName", "feature", "gene" };
}
