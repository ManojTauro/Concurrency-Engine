package dev.conc.p9;

/** Every attempt failed. getCause() is the exception of the LAST attempt. */
public class RetriesExhaustedException extends Exception {
    private final int attempts;

    public RetriesExhaustedException(int attempts, Throwable cause) {
        super("failed after " + attempts + " attempts", cause);
        this.attempts = attempts;
    }

    public int attempts() {
        return attempts;
    }
}
