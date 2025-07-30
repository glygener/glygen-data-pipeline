package uk.ac.ebi.uniprot.glygen.input;

public record FileFetchStatus(String url, String filename, FetchStatus status) {

    public static enum FetchStatus{
        SUCCEEDED,
        FETCHED_NOT_UNCOMPRESSED,
        FAILED
    }
}
