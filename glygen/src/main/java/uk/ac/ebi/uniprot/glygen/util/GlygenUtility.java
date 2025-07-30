package uk.ac.ebi.uniprot.glygen.util;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlygenUtility {

    public static final String FALDO_NS = "http://biohackathon.org/resource/faldo#";
    public static final String GLYGEN_CORE_NS = "https://sparql.glygen.org/ontology/";
    public static final String UNIPROT_CORE_NS = "http://purl.uniprot.org/core/";

    public static final String PROTEIN_PREFIX = "http://purl.uniprot.org/uniprot/";
    public static final String ISOFORM_PREFIX = "http://purl.uniprot.org/isoforms/";
    public static final String CITATION_PREFIX = "http://purl.uniprot.org/citations/";
    public static final String RANGE_PREFIX = "http://purl.uniprot.org/range/";
    public static final String POSITION_PREFIX = "http://purl.uniprot.org/position/";
    public static final String TAXONOMY_PREFIX = "http://purl.uniprot.org/taxonomy/";
    public static final String ENS_PEPTIDE_PREFIX = "http://rdf.ebi.ac.uk/resource/ensembl.protein/";
    public static final String ENS_TRANSCRIPT_PREFIX = "http://rdf.ebi.ac.uk/resource/ensembl.transcript/";
    public static final String PARTICIPANT_PREFIX = "http://purl.uniprot.org/intact/EBI-";
    public static final String ENSG_PREFIX = "http://rdf.ebi.ac.uk/resource/ensembl/";
    public static final String REACTOME_PREFIX = "https://reactome.org/content/detail/"; // Reactome Prefix
    public static final String REACTOME_PB_PREFIX = "https://reactome.org/PathwayBrowser/#/"; // PathwayBrowser Prefix

    public static final String REFSEQ_PROTEIN_PREFIX = "http://rdf.ebi.ac.uk/resource/refseq.protein/";

    public static final char FASTA_SEQ_CHAR_AT_0 = '>';
    public static final char URI_CHAR_SEPARATOR = '_';
    public static final char CHAR_FORWARD_SLASH = '/';
    public static final char CHAR_DASH = '-';
    public static final char CHAR_COLON = ':';
    public static final char CHAR_HASH = '#';
    public static final char CHAR_PLUS = '+';

    public static final String DASH = "-";
    public static final String SP_STR = "sp";
    public static final String CANONICAL_SUFFIX = "-1";

    private static final Pattern UNIPROT_ID_PATTERN = Pattern.compile("^\\w\\w\\|(.*?)\\|");
    private static final Pattern CANONICAL_PATTERN = Pattern.compile(".*Isoform of (\\w+-*\\w+)");

    // co-ordinate constants
    public static final String ACCESSION = "accession";
    public static final String ENSEMBL_TRANSCRIPT_ID = "ensemblTranscriptId";
    public static final String CHROMOSOME = "chromosome";
    public static final String EXON = "exon";
    public static final String BEGIN = "begin";
    public static final String START = "start";
    public static final String END = "end";
    public static final String ID = "id";
    public static final String POSITION = "position";
    public static final String PROTEIN_LOCATION = "proteinLocation";
    public static final String GENOME_LOCATION = "genomeLocation";
    public static final String REVERSE_STRAND = "reverseStrand";
    public static final String REFSEQ_PROTEIN_ID = "refseqProteinId";

    // rdf query constants
    public static final String PROTEIN = "protein";
    public static final String RECOMMENDED_NAME = "recommendedName";
    public static final String ALTERNATIVE_NAME = "alternativeName";
    public static final String SUBMITTED_NAME = "submittedName";
    public static final String ENCODED_BY = "encodedBy";
    public static final String LABEL = "label";
    public static final String EC = "ec";
    public static final String PREF_LABEL = "prefLabel";
    public static final String ALT_LABEL = "altLabel";
    public static final String ORF_NAME = "orfName";
    public static final String ORGANISM = "organism";
    public static final String ISOLATED_FROM = "isolatedFrom";
    public static final String CLASSIFIED_WITH = "classifiedWith";
    public static final String ENZYME = "enzyme";
    public static final String ACTIVITY = "activity";
    public static final String CO_FACTOR_LABEL = "cofactorLabel";
    public static final String SUB_CLASS_OF = "subClassOf";
    public static final String INTERACTION = "interaction";
    public static final String XENO = "xeno";
    public static final String EXPERIMENTS = "experiments";
    public static final String SAME_AS = "sameAs";
    public static final String STRUCTURED_RESOURCE = "structuredResource";
    public static final String DATABASE = "database";
    public static final String METHOD = "method";
    public static final String RESOLUTION = "resolution";
    public static final String COMPONENT = "component";
    public static final String DOMAIN = "domain";
    public static final String ANNOTATION = "annotation";
    public static final String PARTICIPANT = "participant";
    public static final String TYPE = "type";
    public static final String COMMENT = "comment";
    public static final String SEE_ALSO = "seeAlso";
    public static final String RELATED = "related";
    public static final String CO_FACTOR = "cofactor";
    public static final String CONFLICTING_SEQUENCE = "conflictingSequence";
    public static final String DISEASE = "disease";
    public static final String LOCATED_IN = "locatedIn";
    public static final String MEASURED_ERROR = "measuredError";
    public static final String MEASURED_VALUE = "measuredValue";
    public static final String RANGE = "range";
    public static final String SEQUENCE = "sequence";
    public static final String SUBSTITUTION = "substitution";
    public static final String MD5_CHECK_SUM = "md5Checksum";
    public static final String FRAGMENT = "fragment";
    public static final String REVIEWED = "reviewed";
    public static final String PRECURSOR = "precursor";
    public static final String MODIFIED = "modified";
    public static final String MASS = "mass";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String VALUE = "value";
    public static final String CREATED = "created";
    public static final String EXISTENCE = "existence";
    public static final String MNEMONIC = "mnemonic";
    public static final String SINGLE = "single";
    public static final String MULTIPLE = "multiple";
    public static final String ENS_TRANSCRIPT = "ensTranscript";
    public static final String TRANSLATED_TO = "translatedTo";
    public static final String BEGIN_POS = "beginPos";
    public static final String END_POS = "endPos";
    public static final String RESOURCE = "resource";
    public static final String ABBREVIATION = "abbreviation";
    public static final String CATEGORY = "category";
    public static final String URL_TEMPLATE = "urlTemplate";
    public static final String CITATION = "citation";
    public static final String TITLE = "title";
    public static final String COUNT = "count";
    public static final String FULL_NAME = "fullName";
    public static final String SHORT_NAME = "shortName";
    public static final String EC_NAME = "ecName";
    public static final String CHAIN = "chain";
    public static final String CHAIN_SEQUENCE_MAPPING = "chainSequenceMapping";
    public static final String OBSOLETE = "obsolete";
    public static final String REPLACED_BY = "replacedBy";
    public static final String REPLACES = "replaces";
    public static final String NODE = "node";
    public static final String SOURCE = "source";
    public static final String EVIDENCE = "evidence";
    public static final String ATTRIBUTION = "attribution";
    public static final String CELLULAR_COMPONENT = "cellularComponent";
    public static final String DATE = "date";
    public static final String EXACT_MATCH = "exactMatch";
    public static final String IDENTIFIER = "identifier";
    public static final String VOLUME = "volume";
    public static final String PAGES = "pages";
    public static final String AUTHOR = "author";
    public static final String REIF_ID = "reifId";
    public static final String CLOSE_MATCH = "closeMatch";
    public static final String CATALYTIC_ACTIVITY = "catalyticActivity";
    public static final String CATALYZED_REACTION = "catalyzedReaction";
    public static final String CATALYZED_PHYSIOLOGICAL_REACTION = "catalyzedPhysiologicalReaction";
    public static final String LIGAND = "ligand";
    public static final String LIGAND_PART = "ligandPart";

    // output rdf constants
    public static final String FALDO_BEGIN = FALDO_NS + BEGIN;
    public static final String FALDO_END = FALDO_NS + END;
    public static final String FALDO_POSITION = FALDO_NS + POSITION;
    public static final String GLY_CANONICAL = GLYGEN_CORE_NS + "canonical";
    public static final String GLY_CHROMOSOME = GLYGEN_CORE_NS + CHROMOSOME;
    public static final String GLY_REVERSE_STRAND = GLYGEN_CORE_NS + REVERSE_STRAND;
    public static final String GLY_ENS_TRANSCRIPT = GLYGEN_CORE_NS + ENS_TRANSCRIPT;
    public static final String GLY_EXON_RANGE = GLYGEN_CORE_NS + "exonRange";
    public static final String GLY_TRANSCRIPT_RANGE = GLYGEN_CORE_NS + "transcriptRange";
    public static final String GLY_GENE_RANGE = GLYGEN_CORE_NS + "geneRange";
    public static final String GLY_HAS_LOCUS = GLYGEN_CORE_NS + "hasLocus";

    //reactome reaction
    public static final String GLY_RXN_EVIDENCE_CODE = GLYGEN_CORE_NS + "rxnEvidence";
    public static final String GLY_RXN_NAME = GLYGEN_CORE_NS + "rxnName";
    public static final String GLY_RXN_SUMMARY = GLYGEN_CORE_NS + "rxnSummary";
    public static final String GLY_RXN_INPUT = GLYGEN_CORE_NS + "rxnInput";
    public static final String GLY_RXN_OUTPUT = GLYGEN_CORE_NS + "rxnOutput";
    public static final String GLY_RXN_DISEASE = GLYGEN_CORE_NS + "rxnDisease";
    public static final String GLY_PARTICIPANT_NAME = GLYGEN_CORE_NS + "participantName";
    public static final String GLY_DO_NAME = GLYGEN_CORE_NS + "doName";
    public static final String GLY_CELLULAR_LOCATION = GLYGEN_CORE_NS + "cellularLocation";
    public static final String GLY_XREF_IDENTIFIER = GLYGEN_CORE_NS + "xrefIdentifier";
    public static final String GLY_REACTION_DATABASE = GLYGEN_CORE_NS + "reactionDatabase";
    // Xref_Identifier
    public static final String GLY_XREF_ID = GLYGEN_CORE_NS + "xrefId";
    public static final String GLY_XREF_ID_TYPE= GLYGEN_CORE_NS + "xrefIdType";

    public static final String GLY_GO_CLASSIFICATION = GLYGEN_CORE_NS + "goClassification";
    public static final String GLY_PATHWAY_DATABASE = GLYGEN_CORE_NS + "pathwayDatabase";
    public static final String GLY_PATHWAY_NAME = GLYGEN_CORE_NS + "pathwayName";
    public static final String GLY_PATHWAY_SUMMARY = GLYGEN_CORE_NS + "pathwaySummary";
    public static final String GLY_PATHWAY = GLYGEN_CORE_NS + "pathway";
    public static final String GLY_ATTRIBUTION = GLYGEN_CORE_NS + ATTRIBUTION;
    public static final String GLY_CATALYZED_PHYSIOLOGICAL_ACTIVITY = GLYGEN_CORE_NS + "catalyzedPhysiologicalActivity";
    public static final String GLY_EQUATION = GLYGEN_CORE_NS + "equation";
    public static final String GLY_HAS_ENZYME = GLYGEN_CORE_NS + "hasEnzyme";

    public static final String UP_RECOMMENDED_NAME = UNIPROT_CORE_NS + RECOMMENDED_NAME;
    public static final String UP_ALTERNATIVE_NAME = UNIPROT_CORE_NS + ALTERNATIVE_NAME;
    public static final String UP_SUBMITTED_NAME = UNIPROT_CORE_NS + SUBMITTED_NAME;
    public static final String UP_ENCODED_BY = UNIPROT_CORE_NS + ENCODED_BY;
    public static final String UP_ORF_NAME = UNIPROT_CORE_NS + ORF_NAME;
    public static final String UP_ORGANISM = UNIPROT_CORE_NS + ORGANISM;
    public static final String UP_HOST = UNIPROT_CORE_NS + "host";
    public static final String UP_SCIENTIFIC_NAME = UNIPROT_CORE_NS + "scientificName";
    public static final String UP_COMMON_NAME = UNIPROT_CORE_NS + "commonName";
    public static final String UP_ISOLATED_FROM = UNIPROT_CORE_NS + ISOLATED_FROM;
    public static final String UP_CLASSIFIED_WITH = UNIPROT_CORE_NS + CLASSIFIED_WITH;
    public static final String UP_ENZYME = UNIPROT_CORE_NS + ENZYME;
    public static final String UP_ENZYME_CLASS = UNIPROT_CORE_NS + "enzymeClass";
    public static final String UP_ACTIVITY = UNIPROT_CORE_NS + ACTIVITY;
    public static final String UP_CO_FACTOR_LABEL = UNIPROT_CORE_NS + CO_FACTOR_LABEL;
    public static final String UP_INTERACTION = UNIPROT_CORE_NS + INTERACTION;
    public static final String UP_XENO = UNIPROT_CORE_NS + XENO;
    public static final String UP_EXPERIMENTS = UNIPROT_CORE_NS + EXPERIMENTS;
    public static final String UP_PARTICIPANT = UNIPROT_CORE_NS + PARTICIPANT;
    public static final String UP_DATABASE = UNIPROT_CORE_NS + DATABASE;
    public static final String UP_METHOD = UNIPROT_CORE_NS + METHOD;
    public static final String UP_RESOLUTION = UNIPROT_CORE_NS + RESOLUTION;
    public static final String UP_COMPONENT = UNIPROT_CORE_NS + COMPONENT;
    public static final String UP_DOMAIN = UNIPROT_CORE_NS + DOMAIN;
    public static final String UP_ANNOTATION = UNIPROT_CORE_NS + ANNOTATION;
    public static final String UP_CO_FACTOR = UNIPROT_CORE_NS + CO_FACTOR;
    public static final String UP_CONFLICTING_SEQUENCE = UNIPROT_CORE_NS + CONFLICTING_SEQUENCE;
    public static final String UP_DISEASE = UNIPROT_CORE_NS + DISEASE;
    public static final String UP_LOCATED_IN = UNIPROT_CORE_NS + LOCATED_IN;
    public static final String UP_MEASURED_ERROR = UNIPROT_CORE_NS + MEASURED_ERROR;
    public static final String UP_MEASURED_VALUE = UNIPROT_CORE_NS + MEASURED_VALUE;
    public static final String UP_RANGE = UNIPROT_CORE_NS + RANGE;
    public static final String UP_SEQUENCE = UNIPROT_CORE_NS + SEQUENCE;
    public static final String UP_SUBSTITUTION = UNIPROT_CORE_NS + SUBSTITUTION;
    public static final String UP_MD5_CHECK_SUM = UNIPROT_CORE_NS + MD5_CHECK_SUM;
    public static final String UP_FRAGMENT = UNIPROT_CORE_NS + FRAGMENT;
    public static final String UP_MASS = UNIPROT_CORE_NS + MASS;
    public static final String UP_NAME = UNIPROT_CORE_NS + NAME;
    public static final String UP_DATE = UNIPROT_CORE_NS + DATE;
    public static final String UP_AUTHOR = UNIPROT_CORE_NS + AUTHOR;
    public static final String UP_VOLUME = UNIPROT_CORE_NS + VOLUME;
    public static final String UP_PAGES = UNIPROT_CORE_NS + PAGES;
    public static final String UP_TITLE = UNIPROT_CORE_NS + TITLE;
    public static final String UP_PRECURSOR = UNIPROT_CORE_NS + PRECURSOR;
    public static final String UP_TRANSLATED_TO = UNIPROT_CORE_NS + TRANSLATED_TO;
    public static final String UP_ABBREVIATION = UNIPROT_CORE_NS + ABBREVIATION;
    public static final String UP_CATEGORY = UNIPROT_CORE_NS + CATEGORY;
    public static final String UP_URL_TEMPLATE = UNIPROT_CORE_NS + URL_TEMPLATE;
    public static final String UP_CITATION = UNIPROT_CORE_NS + CITATION;
    public static final String UP_VERSION = UNIPROT_CORE_NS + VERSION;
    public static final String UP_CREATED = UNIPROT_CORE_NS + CREATED;
    public static final String UP_MODIFIED = UNIPROT_CORE_NS + MODIFIED;
    public static final String UP_MNEMONIC = UNIPROT_CORE_NS + MNEMONIC;
    public static final String UP_REVIEWED = UNIPROT_CORE_NS + REVIEWED;
    public static final String UP_EXISTENCE = UNIPROT_CORE_NS + EXISTENCE;
    public static final String UP_FULL_NAME = UNIPROT_CORE_NS + FULL_NAME;
    public static final String UP_SHORT_NAME = UNIPROT_CORE_NS + SHORT_NAME;
    public static final String UP_EC_NAME = UNIPROT_CORE_NS + EC_NAME;
    public static final String UP_CHAIN = UNIPROT_CORE_NS + CHAIN;
    public static final String UP_CHAIN_SEQUENCE_MAPPING = UNIPROT_CORE_NS + CHAIN_SEQUENCE_MAPPING;
    public static final String UP_OBSOLETE = UNIPROT_CORE_NS + OBSOLETE;
    public static final String UP_REPLACED_BY = UNIPROT_CORE_NS + REPLACED_BY;
    public static final String UP_REPLACES = UNIPROT_CORE_NS + REPLACES;
    public static final String UP_SOURCE = UNIPROT_CORE_NS + SOURCE;
    public static final String UP_EVIDENCE = UNIPROT_CORE_NS + EVIDENCE;
    public static final String UP_CELLULAR_COMPONENT = UNIPROT_CORE_NS + CELLULAR_COMPONENT;
    public static final String UP_CATALYTIC_ACTIVITY = UNIPROT_CORE_NS + CATALYTIC_ACTIVITY;
    public static final String UP_CATALYZED_REACTION = UNIPROT_CORE_NS + CATALYZED_REACTION ;
    public static final String UP_LIGAND = UNIPROT_CORE_NS + LIGAND;
    public static final String UP_LIGAND_PART = UNIPROT_CORE_NS + LIGAND_PART;

    // type constants
    public static final String GLY_TYPE_TRANSALTED_SEQUENCE = GLYGEN_CORE_NS + "Translated_Sequence";
    public static final String GLY_TYPE_REACTION_PARTICIPANT = GLYGEN_CORE_NS + "Reaction_Participant";
    public static final String GLY_TYPE_DISEASE_ONTOLOGY = GLYGEN_CORE_NS + "Disease_Ontology";
    public static final String GLY_TYPE_GENE_LOCUS = GLYGEN_CORE_NS + "Gene_Locus";
    public static final String GLY_TYPE_X_REF_IDENTIFIER = GLYGEN_CORE_NS + "Xref_Identifier";
    public static final String FALDO_TYPE_REGION = FALDO_NS + "Region";
    public static final String FALDO_TYPE_EXACT_POSITION = FALDO_NS + "ExactPosition";
    public static final String UP_TYPE_GENE = UNIPROT_CORE_NS + "Gene";
    public static final String UP_TYPE_STRUCTURED_NAME = UNIPROT_CORE_NS + "Structured_Name";
    public static final String UP_TYPE_TAXON = UNIPROT_CORE_NS + "Taxon";
    public static final String UP_TYPE_TISSUE = UNIPROT_CORE_NS + "Tissue";
    public static final String UP_TYPE_PROTEIN = UNIPROT_CORE_NS + "Protein";
    public static final String UP_TYPE_CONCEPT = UNIPROT_CORE_NS + "Concept";
    public static final String UP_TYPE_ENZYME = UNIPROT_CORE_NS + "Enzyme";
    public static final String UP_TYPE_PARTICIPANT = UNIPROT_CORE_NS + "Participant";
    public static final String UP_TYPE_INTERACTION = UNIPROT_CORE_NS + "Interaction";
    public static final String UP_TYPE_SELF_INTERACTION = UNIPROT_CORE_NS + "Self_Interaction";
    public static final String UP_TYPE_NON_SELF_INTERACTION = UNIPROT_CORE_NS + "Non_Self_Interaction";
    public static final String UP_TYPE_STRUCTURE_RESOURCE = UNIPROT_CORE_NS + "Structure_Resource";
    public static final String UP_TYPE_PART = UNIPROT_CORE_NS + "Part";
    public static final String UP_TYPE_DISEASE = UNIPROT_CORE_NS + "Disease";
    public static final String UP_TYPE_CELLULAR_COMPONENT = UNIPROT_CORE_NS + "Cellular_Component";
    public static final String UP_TYPE_RESOURCE = UNIPROT_CORE_NS + "Resource";
    public static final String UP_TYPE_TRANSCRIPT_RESOURCE = UNIPROT_CORE_NS + "Transcript_Resource";
    public static final String UP_TYPE_JOURNAL_CITATION = UNIPROT_CORE_NS + "Journal_Citation";
    public static final String UP_TYPE_SIMPLE_SEQUENCE = UNIPROT_CORE_NS + "Simple_Sequence";
    public static final String UP_TYPE_DATABASE = UNIPROT_CORE_NS + "Database";
    public static final String UP_TYPE_CATALYTIC_ACTIVITY = UNIPROT_CORE_NS + "Catalytic_Activity";

    public static final String UP_DATA_TYPE_TOKEN = "http://www.w3.org/2001/XMLSchema#token";

    public static final String REACTOME_DB = "http://purl.uniprot.org/database/Reactome";

    private GlygenUtility() {
    }

    public static String getUniprotIdFromSequenceName(String name) {
        if (name.charAt(0) == FASTA_SEQ_CHAR_AT_0) {
            name = name.substring(1);
        }
        return getMatch(UNIPROT_ID_PATTERN, name);
    }

    public static String getCanonicalFromSequenceName(String name) {
        return getMatch(CANONICAL_PATTERN, name);
    }

    public static String getUri(String pref) {
        String uri = UUID.randomUUID().toString().replace(DASH, "").trim();
        return pref == null ? uri : pref + uri;
    }

    private static String getMatch(Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getAbsFileName(String name) {
        URL resource = GlygenUtility.class.getClassLoader().getResource(name);
        File file = resource == null ? new File(name) : new File(Objects.requireNonNull(resource).getFile());
        return file.getAbsolutePath();
    }

    public static boolean isSpEntry(String name) {
        return name.startsWith(SP_STR);
    }
}
