package dev.conc.p3;

/** Thrown by RoundRunner.run when a task threw; the cause is the first task exception. */
public final class RoundFailedException extends RuntimeException {
    public RoundFailedException(Throwable cause) {
        super(cause);
    }
}
