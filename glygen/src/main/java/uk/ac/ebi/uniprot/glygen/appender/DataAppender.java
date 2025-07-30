package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

public interface DataAppender {
    void appendData(GlygenDataset dataset);
}
