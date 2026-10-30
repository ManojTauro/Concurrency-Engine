package dev.conc.p11;

/** One measurement of an open-loop run: the arrival rate and the p99 latency it produced. */
public record RatePoint(double ratePerSecond, double p99Millis) {
}
