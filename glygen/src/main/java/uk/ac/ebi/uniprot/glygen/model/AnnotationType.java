package uk.ac.ebi.uniprot.glygen.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.GLYGEN_CORE_NS;
import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.UNIPROT_CORE_NS;

public class AnnotationType {

    private AnnotationType(){}

    public static final String AT_ACTIVE_SITE = UNIPROT_CORE_NS + "Active_Site_Annotation";
    public static final String AT_ALTERNATIVE_SEQUENCE = UNIPROT_CORE_NS + "Alternative_Sequence_Annotation";
    public static final String AT_BETA_STRAND = UNIPROT_CORE_NS + "Beta_Strand_Annotation";
    public static final String AT_BINDING_SITE = UNIPROT_CORE_NS + "Binding_Site_Annotation";
    public static final String AT_CATALYTIC_ACTIVITY = UNIPROT_CORE_NS + "Catalytic_Activity_Annotation";
    public static final String AT_CHAIN = UNIPROT_CORE_NS + "Chain_Annotation";
    public static final String AT_CO_FACTOR = UNIPROT_CORE_NS + "Cofactor_Annotation";
    public static final String AT_CROSS_LINK = UNIPROT_CORE_NS + "Cross-link_Annotation";
    public static final String AT_DISEASE = UNIPROT_CORE_NS + "Disease_Annotation";
    public static final String AT_DISRUPTION_PHENOTYPE = UNIPROT_CORE_NS + "Disruption_Phenotype_Annotation";
    public static final String AT_DISULFIDE_BOND = UNIPROT_CORE_NS + "Disulfide_Bond_Annotation";
    public static final String AT_DOMAIN = UNIPROT_CORE_NS + "Domain_Annotation";
    public static final String AT_DOMAIN_EXTENT = UNIPROT_CORE_NS + "Domain_Extent_Annotation";
    public static final String AT_ACTIVITY_REGULATION = UNIPROT_CORE_NS + "Activity_Regulation_Annotation";
    public static final String AT_FUNCTION = UNIPROT_CORE_NS + "Function_Annotation";
    public static final String AT_GLYCOSYLATION = UNIPROT_CORE_NS + "Glycosylation_Annotation";
    public static final String AT_HELIX = UNIPROT_CORE_NS + "Helix_Annotation";
    public static final String AT_INITIATOR_METHIONINE = UNIPROT_CORE_NS + "Initiator_Methionine_Annotation";
    public static final String AT_INTRA_MEMBRANE = UNIPROT_CORE_NS + "Intramembrane_Annotation";
    public static final String AT_LIPIDATION = UNIPROT_CORE_NS + "Lipidation_Annotation";
    public static final String AT_MASS_SPECTROMETRY = UNIPROT_CORE_NS + "Mass_Spectrometry_Annotation";
    public static final String AT_MODIFIED_RESIDUE = UNIPROT_CORE_NS + "Modified_Residue_Annotation";
    public static final String AT_MOTIF = UNIPROT_CORE_NS + "Motif_Annotation";
    public static final String AT_MUTAGENESIS = UNIPROT_CORE_NS + "Mutagenesis_Annotation";
    public static final String AT_NATURAL_VARIANT = UNIPROT_CORE_NS + "Natural_Variant_Annotation";
    public static final String AT_NUCLEOTIDE_BINDING = UNIPROT_CORE_NS + "Nucleotide_Binding_Annotation";
    public static final String AT_PEPTIDE = UNIPROT_CORE_NS + "Peptide_Annotation";
    public static final String AT_POLYMORPHISM = UNIPROT_CORE_NS + "Polymorphism_Annotation";
    public static final String AT_PROPEPTIDE = UNIPROT_CORE_NS + "Propeptide_Annotation";
    public static final String AT_PTM = UNIPROT_CORE_NS + "PTM_Annotation";
    public static final String AT_SEQUENCE_CAUTION = UNIPROT_CORE_NS + "Sequence_Caution_Annotation";
    public static final String AT_SEQUENCE_CONFLICT = UNIPROT_CORE_NS + "Sequence_Conflict_Annotation";
    public static final String AT_SIGNAL_PEPTIDE = UNIPROT_CORE_NS + "Signal_Peptide_Annotation";
    public static final String AT_SITE = UNIPROT_CORE_NS + "Site_Annotation";
    public static final String AT_SUBCELLULAR_LOCATION = UNIPROT_CORE_NS + "Subcellular_Location_Annotation";
    public static final String AT_TURN = UNIPROT_CORE_NS + "Turn_Annotation";
    public static final String AT_SUBUNIT = UNIPROT_CORE_NS + "Subunit_Annotation";

    public static final String AT_PATHWAY = GLYGEN_CORE_NS + "Pathway_Annotation";
    public static final String AT_REACTION = GLYGEN_CORE_NS + "Reaction_Annotation";

    public static final Set<String> ANNOTATION_TYPES;
    public static final Set<String> ANNOTATION_TYPES_WITH_COMMENT_ATTRIB;

    // removed in 2022_03
    //public static final String AT_METAL_BINDING = UNIPROT_CORE_NS + "Metal_Binding_Annotation";
    //public static final String AT_NP_BINDING = UNIPROT_CORE_NS + "NP_Binding_Annotation";
    //public static final String AT_CALCIUM_BINDING = UNIPROT_CORE_NS + "Calcium_Binding_Annotation";

    static {
        HashSet<String> set = new HashSet<>();
        set.add(AT_ACTIVE_SITE);
        set.add(AT_ALTERNATIVE_SEQUENCE);
        set.add(AT_BETA_STRAND);
        set.add(AT_BINDING_SITE);
        set.add(AT_CATALYTIC_ACTIVITY);
        set.add(AT_CHAIN);
        set.add(AT_CO_FACTOR);
        set.add(AT_CROSS_LINK);
        set.add(AT_DISEASE);
        set.add(AT_DISRUPTION_PHENOTYPE);
        set.add(AT_DISULFIDE_BOND);
        set.add(AT_DOMAIN);
        set.add(AT_DOMAIN_EXTENT);
        set.add(AT_ACTIVITY_REGULATION);
        set.add(AT_FUNCTION);
        set.add(AT_GLYCOSYLATION);
        set.add(AT_HELIX);
        set.add(AT_INITIATOR_METHIONINE);
        set.add(AT_INTRA_MEMBRANE);
        set.add(AT_LIPIDATION);
        set.add(AT_MASS_SPECTROMETRY);
        set.add(AT_MODIFIED_RESIDUE);
        set.add(AT_MOTIF);
        set.add(AT_MUTAGENESIS);
        set.add(AT_NATURAL_VARIANT);
        set.add(AT_NUCLEOTIDE_BINDING);
        set.add(AT_PATHWAY);
        set.add(AT_PEPTIDE);
        set.add(AT_POLYMORPHISM);
        set.add(AT_PROPEPTIDE);
        set.add(AT_PTM);
        set.add(AT_SEQUENCE_CAUTION);
        set.add(AT_SEQUENCE_CONFLICT);
        set.add(AT_SIGNAL_PEPTIDE);
        set.add(AT_SITE);
        set.add(AT_SUBCELLULAR_LOCATION);
        set.add(AT_SUBUNIT);
        set.add(AT_TURN);
        set.add(AT_REACTION);
        ANNOTATION_TYPES = Collections.unmodifiableSet(set);

        set = new HashSet<>();
        set.add(AT_ACTIVITY_REGULATION);
        set.add(AT_CO_FACTOR);
        set.add(AT_DISEASE);
        set.add(AT_DOMAIN);
        set.add(AT_FUNCTION);
        set.add(AT_POLYMORPHISM);
        set.add(AT_PTM);
        set.add(AT_SUBCELLULAR_LOCATION);
        set.add(AT_SUBUNIT);
        ANNOTATION_TYPES_WITH_COMMENT_ATTRIB = Collections.unmodifiableSet(set);
    }

}
