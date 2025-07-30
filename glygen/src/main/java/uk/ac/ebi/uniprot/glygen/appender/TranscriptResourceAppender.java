package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getEnsemblTranscriptsFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getSingleSeqEnsemblTranscriptsFromRdf;

public class TranscriptResourceAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GlygenConfig config;
    private Model outModel;
    private final Set<String> transcriptIds = new HashSet<>();
    private final Set<String> peptideIds = new HashSet<>();
    private GlygenDataset dataset;

    public TranscriptResourceAppender(GlygenConfig config) {
        this.config = config;
        if (config.getEnsemblCds() == null || config.getEnsemblPeptide() == null) {
            logger.warn("{}: Skipping Transcript_Resource & Translated_Sequence as ensemblCds ans/or ensemblPeptide " +
                    "is not configured", config.getName());
        } else {
            checkResourceExists(logger, config.getEnsemblCds());
            checkResourceExists(logger, config.getEnsemblPeptide());
        }
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        if (config.getEnsemblCds() == null || config.getEnsemblPeptide() == null) {
            return;
        }

        this.dataset = dataset;
        outModel = dataset.getOutModel();

        updateEnsemblTranscriptFromRdf(getEnsemblTranscriptsFromRdf(dataset.getRdfModel(), config.getTranscriptDb()), false);
        updateEnsemblTranscriptFromRdf(getSingleSeqEnsemblTranscriptsFromRdf(dataset.getRdfModel(), config.getTranscriptDb()), true);

        readFastaEntries(config.getEnsemblCds(), ENS_TRANSCRIPT_PREFIX, transcriptIds);
        readFastaEntries(config.getEnsemblPeptide(), ENS_PEPTIDE_PREFIX, peptideIds);
    }

    private void readFastaEntries(String fileName, String prefix, Set<String> idSet) {
        File file = new File(getAbsFileName(fileName));
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String currLine = fileReader.readLine();

            while (currLine != null) {
                if (currLine.charAt(0) != FASTA_SEQ_CHAR_AT_0) {
                    logger.error("Fasta file format is wrong: {} ", file.getPath());
                    throw new GlyGenException("Fasta file format is wrong: " + file.getPath());
                } else {
                    String nameLine = currLine;
                    StringBuilder seqBuilder = new StringBuilder();
                    do {
                        currLine = fileReader.readLine();
                        if (currLine != null && (currLine.charAt(0) != FASTA_SEQ_CHAR_AT_0)) {
                            seqBuilder.append(currLine);
                        }
                    } while (currLine != null && (currLine.charAt(0) != FASTA_SEQ_CHAR_AT_0));

                    addResource(nameLine, seqBuilder.toString(), prefix, idSet);
                }
            }
        }  catch (IOException ex) {
            throw new GlyGenException("Error appending Ensemble Peptide " + ex);
        }
    }


    private void addResource(String name, String seq, String prefix, Set<String> idSet) {
        String id = name.split(" ")[0].substring(1);

        if (idSet.contains(id)) {
            Resource resource = outModel.createResource(prefix + id);
            resource.addProperty(RDF.value, outModel.createLiteral(seq));
        }
    }

    private void updateEnsemblTranscriptFromRdf(ResultSet resultSet, boolean addPep) {
        Property revProp = outModel.createProperty(UP_REVIEWED);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Resource sequence = outModel.createResource(solution.getResource(SEQUENCE).getURI());

            // Add peptide sequence(value text) only for reviewed isoform and if there is no exact match ie.,
            // ensTranscript::rdfs:seeAlso does not exist.
            String transToUri = solution.getResource(TRANSLATED_TO).getURI();

            Resource peptide =
                    (addPep && sequence.hasProperty(revProp) && sequence.getProperty(revProp).getBoolean()) ?
                            createResource(transToUri, peptideIds) : outModel.createResource(transToUri);
            peptide.addProperty(RDF.type, outModel.createResource(GLY_TYPE_TRANSALTED_SEQUENCE));

            String transId = solution.getResource(ENS_TRANSCRIPT).getURI();
            dataset.trackTranscriptId(transId.substring(transId.lastIndexOf(CHAR_FORWARD_SLASH) + 1));
            Resource ensTranscript = createResource(transId, transcriptIds);
            ensTranscript.addProperty(RDF.type, outModel.createResource(UP_TYPE_TRANSCRIPT_RESOURCE));
            ensTranscript.addProperty(outModel.createProperty(UP_TRANSLATED_TO), peptide);

            sequence.addProperty(outModel.createProperty(GLY_ENS_TRANSCRIPT), ensTranscript);
        }
    }

    private Resource createResource(String uri, Set<String> set) {
        set.add(uri.substring(uri.lastIndexOf(CHAR_FORWARD_SLASH) + 1));
        return outModel.createResource(uri);
    }

    private void checkResourceExists(Logger logger, String fileName) {
        File file = new File(getAbsFileName(fileName));

        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String s = fileReader.readLine();
            if (s == null || s.isEmpty())
                throw new GlyGenException("Empty input file - " + file.getPath());
        } catch (IOException ex) {
            logger.error("Error reading input file - {}", file.getPath(), ex);
            throw new GlyGenException("Error reading input file - " + file.getPath());
        }
    }
}