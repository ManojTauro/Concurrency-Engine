package dev.conc.p9;

/** A failure that retrying cannot fix (a malformed event, a rejected request). Never retried. */
public class PermanentFailureException extends Exception {
    public PermanentFailureException(String message) {
        super(message);
    }

    public PermanentFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
