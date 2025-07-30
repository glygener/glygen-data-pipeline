package uk.ac.ebi.uniprot.glygen.core;

public class GlyGenException extends RuntimeException {

    public GlyGenException(String reason) {
        super(reason);
    }

    public GlyGenException(Exception ex) {
        super(ex);
    }

    public GlyGenException(String msg, Exception ex) {
        super(msg, ex);
    }
}
