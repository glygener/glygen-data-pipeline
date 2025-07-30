package uk.ac.ebi.uniprot.glygen.model;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.getUri;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.CHAR_FORWARD_SLASH;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.DASH;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.FALDO_POSITION;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN_PREFIX;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.POSITION_PREFIX;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.FALDO_TYPE_EXACT_POSITION;

/**
 * Class to hold accession set, proteome model (rdfModel) and output model (outModel)
 */
public class GlygenDataset {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Model rdfModel;
    private final Model outModel;
    private TreeSet<String> canonicalSet = new TreeSet<>();
    private Map<String, Set<String>> accessionMap = new HashMap<>();
    private final Map<String, String> transIdMap = new HashMap<>();
    private final Map<Long, Resource> positionMap = new HashMap<>();
    private final Set<String> dbUriSet = new HashSet<>();

    public GlygenDataset(Model rdfModel, Model outModel) {
        this.rdfModel = rdfModel;
        this.outModel = outModel;
    }

    public Model getOutModel() {
        return outModel;
    }

    public Model getRdfModel() {
        return rdfModel;
    }

    public void createAccessionMap(Set<String> set) {
        accessionMap = new HashMap<>();
        for (String acc : set) {
            accessionMap.put(acc, new HashSet<>());
        }
        canonicalSet = new TreeSet<>();
    }

    public Set<String> getAccessionSet() {
        return accessionMap.keySet();
    }

    public void addIsoformIdToCanonicalSet(String isoId) {
        if (!isoId.contains(DASH)) {
            logger.error("Invalid isoform id {}", isoId);
            throw new GlyGenException("Invalid isoform id {}" + isoId);
        }

        String acc = PROTEIN_PREFIX + isoId.substring(isoId.lastIndexOf(CHAR_FORWARD_SLASH) + 1, isoId.indexOf(DASH));
        if (canonicalSet.contains(isoId)) {
            logger.error("Canonical getting added again {}, for {}", isoId, acc);
            throw new GlyGenException("Canonical getting added again " + isoId);
        }
        mapIsoformIdToAccession(isoId, acc);
        canonicalSet.add(isoId);
    }

    public void mapIsoformIdToAccession(String isoId, String accession) {
        if (!accessionMap.containsKey(accession)) {
            logger.error("Unexpected accession in Dataset {}", accession);
            throw new GlyGenException("Unexpected accession in Dataset " + accession);
        } else {
            accessionMap.get(accession).add(isoId);
        }
    }

    public boolean isValidAccession(String acc) {
        if (accessionMap.containsKey(acc)) return true;
        for (String key : accessionMap.keySet())
            if (accessionMap.get(key).contains(acc))
                return true;
        return false;
    }

    public Set<String> getIsoformsForAccession(String accession) {
        return accessionMap.get(accession);
    }

    public boolean isCanonical(String isoId) {
        return canonicalSet.contains(isoId);
    }

    public int getCanonicalCount() {
        return canonicalSet.size();
    }

    public Resource createPosition(long loc) {
        if (positionMap.containsKey(loc)) {
            return positionMap.get(loc);
        }

        Resource position = outModel.createResource(getUri(POSITION_PREFIX));
        position.addProperty(RDF.type, outModel.createResource(FALDO_TYPE_EXACT_POSITION));
        position.addProperty(outModel.createProperty(FALDO_POSITION), outModel.createTypedLiteral(loc));

        positionMap.put(loc, position);

        return position;
    }

    public void addDbUri(String uri) {
        dbUriSet.add(uri);
    }

    public Set<String> getDbUriSet() {
        return dbUriSet;
    }

    public void trackTranscriptId(String transId) {
        String key = transId.contains(".") ? transId.substring(0, transId.indexOf(".")) : transId;
        transIdMap.put(key, transId);
    }

    public String getTranscriptId(String key){
        return transIdMap.get(key);
    }
}
