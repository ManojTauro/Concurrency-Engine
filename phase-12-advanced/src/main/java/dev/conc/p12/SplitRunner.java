package dev.conc.p12;

/** A unit of work that can be run in short slices, like a Trino Driver. */
@FunctionalInterface
public interface SplitRunner {
    /** Do at most about {@code quantumNanos} of work and say how it ended. */
    SplitResult process(long quantumNanos) throws Exception;
}
