package dev.conc.p9;

/** The breaker refused to call the action. */
public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException() {
        super("circuit is open");
    }
}
