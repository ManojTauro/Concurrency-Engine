package dev.conc.p11;

/** Where a measured latency, in nanoseconds, goes. */
@FunctionalInterface
public interface Recorder {
    void record(long nanos);
}
