package dev.conc.p11;

/** Challenge 4, part 2. Models that explain the sweep. Pure functions. */
public final class Scalability {

    private Scalability() {
    }

    /** Amdahl's law: speedup with n workers when a fraction p (0..1) of the work is parallel: 1 / ((1 - p) + p / n). */
    public static double amdahlSpeedup(double parallelFraction, int n) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Gunther's Universal Scalability Law: throughput with n workers, given the throughput lambda of
     * one worker, the contention sigma and the coherency (crosstalk) cost kappa:
     * lambda * n / (1 + sigma * (n - 1) + kappa * n * (n - 1)).
     */
    public static double uslThroughput(double lambda, double sigma, double kappa, int n) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * The whole number of workers n >= 1 at which uslThroughput is highest. The continuous peak is at
     * sqrt((1 - sigma) / kappa), but the answer must be the better of the two whole numbers around
     * it. If kappa is 0 there is no peak: throw IllegalArgumentException. If sigma >= 1 the answer is 1.
     */
    public static int uslPeakWorkers(double sigma, double kappa) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Little's law: average number in the system L = arrivals per second * seconds per request. */
    public static double littlesLawL(double arrivalsPerSecond, double secondsPerRequest) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
