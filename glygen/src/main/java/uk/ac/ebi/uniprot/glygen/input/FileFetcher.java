package uk.ac.ebi.uniprot.glygen.input;

import java.util.List;

public interface FileFetcher {
    List<FileFetchStatus> fetch();
}
