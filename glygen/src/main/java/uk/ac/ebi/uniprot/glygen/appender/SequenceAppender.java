package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Flag;
import uk.ac.ebi.kraken.util.MessageDigestUtil;
import uk.ac.ebi.kraken.util.fasta.FastaReader;
import uk.ac.ebi.uniprot.dataservice.client.Client;
import uk.ac.ebi.uniprot.dataservice.client.QueryResult;
import uk.ac.ebi.uniprot.dataservice.client.ServiceFactory;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.QuerySpec;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtQueryBuilder;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtService;
import uk.ac.ebi.uniprot.dataservice.query.Query;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.addLiteralIfExists;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getAllSequenceNamesFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getCanonicalSequenceIdFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getSequenceInfoFromRdf;

public class SequenceAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GlygenConfig config;
    private Model rdfModel;
    private Model outModel;
    private GlygenDataset dataset;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SequenceAppender(GlygenConfig config) {
        this.config = config;
        checkInputFileExists(config.getCanonical());
        if (config.getIsoform() != null) {
            checkInputFileExists(config.getIsoform());
        }
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        this.dataset = dataset;
        rdfModel = dataset.getRdfModel();
        outModel = dataset.getOutModel();

        String[] seqIds = updateAllSequenceMappings();
        updateSequenceInfoFromRdf();
        if (config.getIsoform() != null && seqIds != null && seqIds.length > 0) {
            updateSequenceInfoFromUniprotService(seqIds);
        }
        updateAllSequenceNames();
        appendSequencesToProteins();
    }

    private String[] updateAllSequenceMappings() {
        String[] seqIds = null;
        try {
            readSequenceIds(getAbsFileName(config.getCanonical()), true);
            if (config.getIsoform() != null) {
                seqIds = readSequenceIds(getAbsFileName(config.getIsoform()), false);
            }
        } catch (IOException ex) {
            throw new GlyGenException("Error appending Key Sequence Info " + ex);
        }
        return seqIds;
    }

    private String[] readSequenceIds(String fastaFileName, boolean canonical) throws IOException {
        Set<String> seqIdSet = new HashSet<>();
        try (FastaReader fastaReader = new FastaReader(fastaFileName)) {
            fastaReader.openInputFile();

            Set<String> accSet = dataset.getAccessionSet();
            FastaReader.Sequence sequence;
            while ((sequence = fastaReader.nextSequence()) != null) {
                String accId = getUniprotIdFromSequenceName(sequence.name);
                String checkId = PROTEIN_PREFIX + (accId.contains(DASH) ?
                                                           accId.substring(0, accId.indexOf(DASH)) : accId);
                if (accSet.contains(checkId)) {
                    if (canonical) {
                        readCanonicals(sequence, accId);
                    } else {
                        readIsoforms(sequence, accId, seqIdSet);
                    }
                    if (sequence.getSeqLength() == 0) {
                        logger.info(" Sequence length zero for {}", accId);
                    }
                } else {
                    // when accession is in proteome fasta, but not in RDF
                    logger.info("Accession in fasta, but missing in input RDF {}", accId);
                }
            }
        }
        return seqIdSet.toArray(new String[0]);
    }

    private void readCanonicals(FastaReader.Sequence sequence, String accId) {
        String id = isSpEntry(sequence.name) ?
                getCanonicalSequenceIdFromRdf(rdfModel, accId) : accId + CANONICAL_SUFFIX;

        if (id == null) {
            logger.error("Canonical Sequence id not found for {}", accId);
            throw new GlyGenException("Canonical Sequence id not found for " + accId);

        } else {
            // if canonical from RDF, will have full uri
            if (id.contains(String.valueOf(CHAR_FORWARD_SLASH))) {
                id = id.substring(id.lastIndexOf(CHAR_FORWARD_SLASH) + 1);
            }
            dataset.addIsoformIdToCanonicalSet(id);
        }
    }

    private void readIsoforms(FastaReader.Sequence sequence, String isoId, Set<String> seqIdSet) {
        String id = isoId;
        if (isSpEntry(sequence.name)) {
            if (!isoId.contains(DASH)) {
                isoId = getCanonicalSequenceIdFromRdf(rdfModel, isoId);
                if (isoId == null) {
                    logger.error("Canonical Sequence id not found for {}", id);
                    throw new GlyGenException("Canonical Sequence id not found for " + id);
                }
            } else {
                // set of ids whose seq info will be obtained from uniprot service
                seqIdSet.add(isoId);
            }
        } else {
            isoId += CANONICAL_SUFFIX;
        }


        String accession = getCanonicalFromSequenceName(sequence.name);
        if (id.contains(DASH)) {
            id = id.substring(0, id.indexOf(DASH));
        }

        // temp fix for read-throughs. Currently tr readthroughs in .fasta are moved to additional.fasta file & their
        // isoforms still refer to them. In this case canonical
        if (dataset.isCanonical(accession + CANONICAL_SUFFIX)) {
            dataset.mapIsoformIdToAccession(isoId, PROTEIN_PREFIX + accession);

            if (!isoId.contains(accession)) {
                dataset.mapIsoformIdToAccession(isoId, PROTEIN_PREFIX + id);
            }
        } else {
            dataset.mapIsoformIdToAccession(isoId, PROTEIN_PREFIX + id);
        }
    }

    private void updateSequenceInfoFromRdf() {
        ResultSet resultSet = getSequenceInfoFromRdf(rdfModel);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            String seqId = solution.getResource(SEQUENCE).getURI();
            Resource sequence = outModel.createResource(seqId);
            sequence.addProperty(RDF.type, outModel.createResource(UP_TYPE_SIMPLE_SEQUENCE));
            sequence.addProperty(outModel.createProperty(UP_MODIFIED), solution.getLiteral(MODIFIED));
            sequence.addProperty(outModel.createProperty(UP_VERSION),
                    outModel.createTypedLiteral(solution.getLiteral(VERSION).getInt()));
            sequence.addProperty(outModel.createProperty(UP_MASS), solution.getLiteral(MASS));
            sequence.addProperty(outModel.createProperty(UP_MD5_CHECK_SUM), solution.getLiteral(MD5_CHECK_SUM));

            addLiteralIfExists(sequence, solution, outModel.createProperty(UP_PRECURSOR), PRECURSOR);
            addLiteralIfExists(sequence, solution, outModel.createProperty(UP_FRAGMENT), FRAGMENT);

            boolean canonical = dataset.isCanonical(seqId.substring(seqId.lastIndexOf(CHAR_FORWARD_SLASH) + 1));
            sequence.addProperty(outModel.createProperty(GLY_CANONICAL), outModel.createTypedLiteral(canonical));
            sequence.addProperty(outModel.createProperty(UP_REVIEWED), solution.getLiteral(REVIEWED));
            sequence.addProperty(RDF.value, solution.getLiteral(VALUE));
        }
    }

    private void updateSequenceInfoFromUniprotService(String[] seqIds) {
        logger.debug("Number of accessions to get from UniProtService {}", seqIds.length);
        ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
        UniProtService uniProtService = serviceFactoryInstance.getUniProtQueryService();
        try {
            // start the service
            uniProtService.start();

            int cnt = 0 ;
            while (cnt < seqIds.length) {
                int i = 0;
                Set<String> querySet = new HashSet<>();
                while (i++ < 50 && cnt < seqIds.length) {
                    querySet.add(seqIds[cnt++]);
                }
                Query query = UniProtQueryBuilder.accessions(querySet);
                QueryResult<UniProtEntry> searchResult;
                searchResult = uniProtService.getEntries(query, EnumSet.of(QuerySpec.WithIsoform));

                Set<String> retrievedIds = new HashSet<>();
                while (searchResult.hasNext()) {
                    UniProtEntry entry = searchResult.next();
                    String isoId = entry.getPrimaryUniProtAccession().getValue();
                    updateSequenceProperties(outModel, entry, ISOFORM_PREFIX + isoId);
                    retrievedIds.add(isoId);
                }
                querySet.removeAll(retrievedIds);
                if (!querySet.isEmpty()) {
                    logger.debug("Could not retrieve UniProtEntry(s), {}", querySet);
                }
            }
        } catch (ServiceException se) {
            logger.debug("Error getting info from UniProt Service", se);
            throw new GlyGenException(se);
        } finally {
            // always remember to stop the service
            uniProtService.stop();
            logger.debug("UniProtService stopped.");
        }
    }

    private void updateSequenceProperties(Model model, UniProtEntry entry, String seqId) {
        Resource sequence = model.createResource(seqId);
        sequence.addProperty(RDF.type, model.createResource(UP_TYPE_SIMPLE_SEQUENCE));
        sequence.addProperty(model.createProperty(UP_MODIFIED),
                model.createTypedLiteral(dateFormat.format(entry.getEntryAudit().getLastAnnotationUpdateDate()),
                        XSDDatatype.XSDdate));
        sequence.addProperty(model.createProperty(UP_VERSION),
                model.createTypedLiteral(entry.getEntryAudit().getSequenceVersion()));
        sequence.addProperty(model.createProperty(UP_MASS),
                model.createTypedLiteral(entry.getSequence().getMolecularWeight()));
        sequence.addProperty(model.createProperty(UP_MD5_CHECK_SUM),
                model.createTypedLiteral(MessageDigestUtil.getMD5(entry.getSequence().getValue()), UP_DATA_TYPE_TOKEN));

        List<Flag> flagList = entry.getProteinDescription().getFlags();
        for (Flag flag : flagList) {
            switch (flag.getFlagType()) {
                case FRAGMENT:
                    sequence.addProperty(model.createProperty(UP_FRAGMENT), model.createLiteral(SINGLE));
                    break;
                case FRAGMENTS:
                    sequence.addProperty(model.createProperty(UP_FRAGMENT), model.createLiteral(MULTIPLE));
                    break;
                case PRECURSOR:
                    sequence.addProperty(model.createProperty(UP_PRECURSOR), model.createTypedLiteral(true));
                    break;
            }
        }

        // all canonical properties in rdf. This is definitely an isoform
        sequence.addProperty(model.createProperty(GLY_CANONICAL), model.createTypedLiteral(false));

        // only sp isoform properties retrieved from uniprot service
        sequence.addProperty(model.createProperty(UP_REVIEWED), model.createTypedLiteral(true));
        sequence.addProperty(RDF.value, model.createLiteral(entry.getSequence().getValue()));
    }

    private void updateAllSequenceNames() {
        ResultSet resultSet = getAllSequenceNamesFromRdf(rdfModel);
        String uri = "";
        Resource sequence = null;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            // sequence will not be null as initial uri is empty string
            if (!solution.getResource(SEQUENCE).getURI().equals(uri)) {
                uri = solution.getResource(SEQUENCE).getURI();
                sequence = outModel.createResource(uri);
            }
            Objects.requireNonNull(sequence).addProperty(outModel.createProperty(UP_NAME), solution.getLiteral(NAME));
        }
    }

    private void appendSequencesToProteins() {
        Set<String> missingIsoSet = new HashSet<>();
        for (String accession : dataset.getAccessionSet()) {
            Resource protein = outModel.createResource(accession);
            Set<String> isoSet = dataset.getIsoformsForAccession(accession);
            if (isoSet.isEmpty()) {
                // When accession is in RDF but not in proteome fasta
                missingIsoSet.add(accession);
            } else {
                for (String seqId : dataset.getIsoformsForAccession(accession)) {
                    protein.addProperty(outModel.createProperty(UP_SEQUENCE),
                            outModel.createResource(ISOFORM_PREFIX + seqId));
                }
            }
        }
        if (!missingIsoSet.isEmpty()) {
            logger.debug("Accessions in RDF, but not in proteome additional fasta: {}", missingIsoSet);
        }
    }

    private void checkInputFileExists(String fileName) {
        try (FastaReader fastaReader = new FastaReader(getAbsFileName(fileName))) {
            fastaReader.openInputFile();
        } catch (IOException ex) {
            logger.error("Error reading file - {}", fileName, ex);
            throw new GlyGenException("Error reading file - " + fileName);
        }
    }
}
