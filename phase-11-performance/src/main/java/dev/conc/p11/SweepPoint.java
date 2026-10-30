package dev.conc.p11;

/** One row of the worker sweep. Latencies are sojourn times: from submission to completion. */
public record SweepPoint(int workers, double throughputPerSecond, long p50Nanos, long p99Nanos) {
}
