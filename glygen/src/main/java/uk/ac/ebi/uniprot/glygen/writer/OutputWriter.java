package uk.ac.ebi.uniprot.glygen.writer;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import org.apache.jena.rdf.model.Model;

public interface OutputWriter {
    void writeOutput(GlygenConfig config, Model model);
}
