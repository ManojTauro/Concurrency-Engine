package dev.conc.p4;

/** Challenge 3, part 1: pool-sizing rules of thumb as small pure functions. */
public final class Sizing {

    private Sizing() {
    }

    /** Threads for work that never waits: one more than the cores (JCIP ch. 8.2). */
    public static int cpuBound(int cores) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Threads for work that waits: cores * targetUtilisation * (1 + waitTime / computeTime), rounded
     * UP, and never below 1. targetUtilisation is in (0, 1].
     */
    public static int blocking(int cores, double targetUtilisation, double waitNanos, double computeNanos) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Little's law: the average number of requests in the system, L = arrivals per second * seconds per request, rounded UP. */
    public static int littlesLaw(double arrivalsPerSecond, double secondsPerRequest) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
