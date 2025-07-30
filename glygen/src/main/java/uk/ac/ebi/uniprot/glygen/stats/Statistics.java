package uk.ac.ebi.uniprot.glygen.stats;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.jena.vocabulary.*;

import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.ANNOTATION_TYPES;
import static uk.ac.ebi.uniprot.glygen.model.AnnotationType.AT_GLYCOSYLATION;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;

@JsonSerialize(using = StatisticsSerializer.class)
public class Statistics {

    public static class StatKey implements Comparable<StatKey> {
        private String clas;
        private String pred;
        private String value;
        private String display;

        StatKey(String clas, String pred, String value, String display) {
            this.clas = clas;
            this.pred = pred;
            this.value = value;
            this.display = display;
        }

        public String getClas() {
            return clas;
        }

        public String getPred() {
            return pred;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int compareTo(StatKey o) {
            return display.compareTo(o.display);
        }

        @Override public String toString() {
            return display;
        }
    }

    private static final String KEY_DELIMITER = ":";

    private TreeMap<StatKey, Long> classMap = new TreeMap<>();
    private TreeMap<StatKey, Long> predicateMap = new TreeMap<>();
    private TreeMap<StatKey, Long> classPredMap = new TreeMap<>();
    private TreeMap<StatKey, Long> classPredValueMap = new TreeMap<>();
    private TreeMap<String, String> nsMap = new TreeMap<>();
    private TreeMap<String, String> nsRevMap = new TreeMap<>();

    public Statistics() {
        populateNsMap();
        populateNsRevMap();
        populateClassMap();
        populatePredicateMap();
        populateClassPredicateMap();
        populateClassPredicateValueMap();
    }

    private StatKey getClassKey(String cls) {
        return new StatKey(cls, null, null, getDisplayString(cls));
    }

    private StatKey getPredKey(String pred) {
        return new StatKey(null, pred, null, getDisplayString(pred));
    }

    private StatKey getClasPredKey(String cls, String pred) {
        return new StatKey(cls, pred, null, getDisplayString(cls) + KEY_DELIMITER + getDisplayString(pred));
    }

    private StatKey getClasPredValueKey(String cls, String pred, String val, String disp) {
        return new StatKey(cls, pred, val, disp);
    }

    private StatKey getClasPredValueKey(String cls, String pred, boolean val, String disp) {
        return new StatKey(cls, pred, Boolean.toString(val), disp);
    }

    private String getDisplayString(String str) {
        String key;
        String rem;
        if (str.indexOf('#') > -1) {
            key = str.substring(0, str.indexOf('#') + 1);
            rem = str.substring(str.indexOf('#') + 1);
        } else {
            key = str.substring(0, str.lastIndexOf('/') + 1);
            rem = str.substring(str.lastIndexOf('/') + 1);
        }
        return nsRevMap.get(key) + rem;
    }

    private void addClassMapEntry(String key) {
        classMap.put(getClassKey(key), 0L);
    }

    private void populateClassMap() {
        for (String type : ANNOTATION_TYPES) {
            addClassMapEntry(type);
        }

        addClassMapEntry(FALDO_TYPE_EXACT_POSITION);
        addClassMapEntry(FALDO_TYPE_REGION);
        addClassMapEntry(OWL.Class.getURI());

        addClassMapEntry(GLY_TYPE_TRANSALTED_SEQUENCE);
        addClassMapEntry(GLY_TYPE_REACTION_PARTICIPANT);
        addClassMapEntry(GLY_TYPE_DISEASE_ONTOLOGY);
        addClassMapEntry(GLY_TYPE_GENE_LOCUS);
        addClassMapEntry(GLY_TYPE_X_REF_IDENTIFIER);

        addClassMapEntry(UP_TYPE_CELLULAR_COMPONENT);
        addClassMapEntry(UP_TYPE_CONCEPT);
        addClassMapEntry(UP_TYPE_DATABASE);
        addClassMapEntry(UP_TYPE_ENZYME);
        addClassMapEntry(UP_TYPE_GENE);
        addClassMapEntry(UP_TYPE_INTERACTION);
        addClassMapEntry(UP_TYPE_JOURNAL_CITATION);
        addClassMapEntry(UP_TYPE_NON_SELF_INTERACTION);
        addClassMapEntry(UP_TYPE_PART);
        addClassMapEntry(UP_TYPE_PARTICIPANT);
        addClassMapEntry(UP_TYPE_RESOURCE);
        addClassMapEntry(UP_TYPE_SELF_INTERACTION);
        addClassMapEntry(UP_TYPE_SIMPLE_SEQUENCE);
        addClassMapEntry(UP_TYPE_STRUCTURE_RESOURCE);
        addClassMapEntry(UP_TYPE_STRUCTURED_NAME);
        addClassMapEntry(UP_TYPE_TAXON);
        addClassMapEntry(UP_TYPE_TISSUE);
        addClassMapEntry(UP_TYPE_TRANSCRIPT_RESOURCE);
        addClassMapEntry(UP_TYPE_PROTEIN);
        addClassMapEntry(UP_TYPE_CATALYTIC_ACTIVITY);
    }

    private void addPredicateMapEntry(String key) {
        predicateMap.put(getPredKey(key), 0L);
    }

    private void populatePredicateMap() {
        addPredicateMapEntry(FALDO_BEGIN);
        addPredicateMapEntry(FALDO_END);
        addPredicateMapEntry(FALDO_POSITION);

        addPredicateMapEntry(GLY_ATTRIBUTION);
        addPredicateMapEntry(GLY_CANONICAL);
        addPredicateMapEntry(GLY_CHROMOSOME);
        addPredicateMapEntry(GLY_ENS_TRANSCRIPT);
        addPredicateMapEntry(GLY_EXON_RANGE);
        addPredicateMapEntry(GLY_REVERSE_STRAND);
        addPredicateMapEntry(GLY_TRANSCRIPT_RANGE);
        addPredicateMapEntry(GLY_GENE_RANGE);
        addPredicateMapEntry(GLY_HAS_LOCUS);
        addPredicateMapEntry(GLY_RXN_EVIDENCE_CODE);
        addPredicateMapEntry(GLY_RXN_NAME);
        addPredicateMapEntry(GLY_RXN_INPUT);
        addPredicateMapEntry(GLY_RXN_OUTPUT);
        addPredicateMapEntry(GLY_RXN_DISEASE);
        addPredicateMapEntry(GLY_PARTICIPANT_NAME);
        addPredicateMapEntry(GLY_DO_NAME);
        addPredicateMapEntry(GLY_GO_CLASSIFICATION);
        addPredicateMapEntry(GLY_CELLULAR_LOCATION);
        addPredicateMapEntry(GLY_RXN_SUMMARY);
        addPredicateMapEntry(GLY_PATHWAY_DATABASE);
        addPredicateMapEntry(GLY_PATHWAY_NAME);
        addPredicateMapEntry(GLY_PATHWAY_SUMMARY);
        addPredicateMapEntry(GLY_PATHWAY);
        addPredicateMapEntry(GLY_XREF_IDENTIFIER);
        addPredicateMapEntry(GLY_XREF_ID);
        addPredicateMapEntry(GLY_XREF_ID_TYPE);
        addPredicateMapEntry(GLY_EQUATION);
        addPredicateMapEntry(GLY_HAS_ENZYME);

        addPredicateMapEntry(OWL.sameAs.getURI());
        addPredicateMapEntry(RDF.value.getURI());
        addPredicateMapEntry(RDFS.comment.getURI());
        addPredicateMapEntry(RDFS.label.getURI());
        addPredicateMapEntry(RDFS.seeAlso.getURI());
        addPredicateMapEntry(RDFS.subClassOf.getURI());
        addPredicateMapEntry(SKOS.altLabel.getURI());
        addPredicateMapEntry(SKOS.prefLabel.getURI());
        addPredicateMapEntry(SKOS.exactMatch.getURI());
        addPredicateMapEntry(DCTerms.identifier.getURI());

        addPredicateMapEntry(UP_ABBREVIATION);
        addPredicateMapEntry(UP_ACTIVITY);
        addPredicateMapEntry(UP_ALTERNATIVE_NAME);
        addPredicateMapEntry(UP_CATEGORY);
        addPredicateMapEntry(UP_CHAIN);
        addPredicateMapEntry(UP_CHAIN_SEQUENCE_MAPPING);
        addPredicateMapEntry(UP_CITATION);
        addPredicateMapEntry(UP_CLASSIFIED_WITH);
        addPredicateMapEntry(UP_CO_FACTOR_LABEL);
        addPredicateMapEntry(UP_COMMON_NAME);
        addPredicateMapEntry(UP_COMPONENT);
        addPredicateMapEntry(UP_DOMAIN);
        addPredicateMapEntry(UP_MD5_CHECK_SUM);
        addPredicateMapEntry(UP_CREATED);
        addPredicateMapEntry(UP_DATABASE);
        addPredicateMapEntry(UP_EC_NAME);
        addPredicateMapEntry(UP_ENCODED_BY);
        addPredicateMapEntry(UP_ENZYME);
        addPredicateMapEntry(UP_EXISTENCE);
        addPredicateMapEntry(UP_EXPERIMENTS);
        addPredicateMapEntry(UP_FRAGMENT);
        addPredicateMapEntry(UP_FULL_NAME);
        addPredicateMapEntry(UP_HOST);
        addPredicateMapEntry(UP_INTERACTION);
        addPredicateMapEntry(UP_ISOLATED_FROM);
        addPredicateMapEntry(UP_LIGAND);
        addPredicateMapEntry(UP_LIGAND_PART);
        addPredicateMapEntry(UP_LOCATED_IN);
        addPredicateMapEntry(UP_MASS);
        addPredicateMapEntry(UP_METHOD);
        addPredicateMapEntry(UP_MNEMONIC);
        addPredicateMapEntry(UP_MODIFIED);
        addPredicateMapEntry(UP_NAME);
        addPredicateMapEntry(UP_OBSOLETE);
        addPredicateMapEntry(UP_ORF_NAME);
        addPredicateMapEntry(UP_ORGANISM);
        addPredicateMapEntry(UP_PARTICIPANT);
        addPredicateMapEntry(UP_PRECURSOR);
        addPredicateMapEntry(UP_RECOMMENDED_NAME);
        addPredicateMapEntry(UP_REPLACED_BY);
        addPredicateMapEntry(UP_REPLACES);
        addPredicateMapEntry(UP_RESOLUTION);
        addPredicateMapEntry(UP_REVIEWED);
        addPredicateMapEntry(UP_SCIENTIFIC_NAME);
        addPredicateMapEntry(UP_SEQUENCE);
        addPredicateMapEntry(UP_SHORT_NAME);
        addPredicateMapEntry(UP_SUBMITTED_NAME);
        addPredicateMapEntry(UP_TITLE);
        addPredicateMapEntry(UP_TRANSLATED_TO);
        addPredicateMapEntry(UP_URL_TEMPLATE);
        addPredicateMapEntry(UP_VERSION);
        addPredicateMapEntry(UP_XENO);
        addPredicateMapEntry(UP_DATE);
        addPredicateMapEntry(UP_RANGE);
        addPredicateMapEntry(UP_VOLUME);
        addPredicateMapEntry(UP_PAGES);
        addPredicateMapEntry(UP_AUTHOR);
        addPredicateMapEntry(UP_ENZYME_CLASS);
        addPredicateMapEntry(UP_CATALYTIC_ACTIVITY);
        addPredicateMapEntry(UP_CATALYZED_REACTION);
        addPredicateMapEntry(UP_EVIDENCE);
        addPredicateMapEntry(UP_SOURCE);
    }

    private void populateClassPredicateMap() {
        classPredMap.put(getClasPredKey(AT_GLYCOSYLATION, UP_RANGE), 0L);
        classPredMap.put(getClasPredKey(AT_GLYCOSYLATION, GLY_ATTRIBUTION), 0L);
        classPredMap.put(getClasPredKey(UP_TYPE_PROTEIN, UP_ENCODED_BY), 0L);
        classPredMap.put(getClasPredKey(UP_TYPE_GENE, SKOS.prefLabel.getURI()), 0L);
        classPredMap.put(getClasPredKey(UP_TYPE_SIMPLE_SEQUENCE, RDF.value.getURI()), 0L);
    }

    private void populateClassPredicateValueMap() {
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_PROTEIN, UP_REVIEWED, true,
                "up:Protein:up:reviewed:xsd:boolean:true"), 0L);
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_SIMPLE_SEQUENCE, UP_REVIEWED, true,
                "up:Simple_Sequence:up:reviewed:xsd:boolean:true"), 0L);
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_SIMPLE_SEQUENCE, GLY_CANONICAL, true,
                "up:Simple_Sequence:up:canonical:xsd:boolean:true"), 0L);
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_PROTEIN, UP_REVIEWED, false,
                "up:Protein:up:reviewed:xsd:boolean:false"), 0L);
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_SIMPLE_SEQUENCE, UP_REVIEWED, false,
                "up:Simple_Sequence:up:reviewed:xsd:boolean:false"), 0L);
        classPredValueMap.put(getClasPredValueKey(UP_TYPE_SIMPLE_SEQUENCE, GLY_CANONICAL, false,
                "up:Simple_Sequence:up:canonical:xsd:boolean:false"), 0L);

        // structure_resource types
        classPredValueMap.put(getStructureResourceStatKey("Electron_Microscopy"), 0L);
        classPredValueMap.put(getStructureResourceStatKey("Fiber_Diffraction"), 0L);
        classPredValueMap.put(getStructureResourceStatKey("Infrared_Spectroscopy"), 0L);
        classPredValueMap.put(getStructureResourceStatKey("NMR_Spectroscopy"), 0L);
        classPredValueMap.put(getStructureResourceStatKey("Neutron_Diffraction"),0L);
        classPredValueMap.put(getStructureResourceStatKey("Prediction"), 0L);
        classPredValueMap.put(getStructureResourceStatKey("X-Ray_Crystallography"),0L);

        // protein_existence types
        classPredValueMap.put(getProteinExistenceStatKey("Evidence_at_Protein_Level_Existence"), 0L);
        classPredValueMap.put(getProteinExistenceStatKey("Evidence_at_Transcript_Level_Existence"), 0L);
        classPredValueMap.put(getProteinExistenceStatKey("Inferred_from_Homology_Existence"), 0L);
        classPredValueMap.put(getProteinExistenceStatKey("Predicted_Existence"), 0L);
        classPredValueMap.put(getProteinExistenceStatKey("Uncertain_Existence"), 0L);
    }

    private StatKey getStructureResourceStatKey(String str) {
        return getClasPredValueKey(UP_TYPE_STRUCTURE_RESOURCE, UP_METHOD, getTagedCoreNsValue(str),
                "up:Structure_Resource:up:method:up:" + str);
    }

    private String getTagedCoreNsValue(String str) {
        return "<" + UNIPROT_CORE_NS + str + ">";
    }

    private StatKey getProteinExistenceStatKey(String str) {
        return getClasPredValueKey(UP_TYPE_PROTEIN, UP_EXISTENCE, getTagedCoreNsValue(str),
                "up:Protein:up:existence:up:" + str);
    }

    private void populateNsMap() {
        nsMap.put("up", UNIPROT_CORE_NS);
        nsMap.put("skos", SKOS.getURI());
        nsMap.put("rdf", RDF.getURI());
        nsMap.put("owl", OWL.getURI());
        nsMap.put("xsd", XSD.getURI());
        nsMap.put("rdfs", RDFS.getURI());
        nsMap.put("faldo", FALDO_NS);
        nsMap.put("gly", GLYGEN_CORE_NS);
        nsMap.put("dcterms", DCTerms.getURI());
    }

    private void populateNsRevMap() {
        nsRevMap.put(UNIPROT_CORE_NS, "up:");
        nsRevMap.put(SKOS.getURI(), "skos:");
        nsRevMap.put(RDF.getURI(), "rdf:");
        nsRevMap.put(OWL.getURI(), "owl:");
        nsRevMap.put(XSD.getURI(), "xsd:");
        nsRevMap.put(RDFS.getURI(), "rdfs:");
        nsRevMap.put(FALDO_NS, "faldo:");
        nsRevMap.put(GLYGEN_CORE_NS, "gly:");
        nsRevMap.put(DCTerms.getURI(), "dcterms:");
    }

    public SortedMap<StatKey, Long> getClassMap() {
        return classMap;
    }

    public SortedMap<StatKey, Long> getPredicateMap() {
        return predicateMap;
    }

    public SortedMap<StatKey, Long> getClassPredicateMap() {
        return classPredMap;
    }

    public SortedMap<StatKey, Long> getClassPredicateValueMap() {
        return classPredValueMap;
    }

    TreeMap<String, String> getNsMap() {
        return nsMap;
    }
}
