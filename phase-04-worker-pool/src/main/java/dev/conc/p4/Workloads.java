package dev.conc.p4;

/** Given. Two kinds of task to size a pool for. */
public final class Workloads {

    private Workloads() {
    }

    private static volatile long sink;

    /** Burns CPU and never waits. */
    public static Runnable cpuBurn(int iterations) {
        return () -> {
            long x = 1;
            for (int i = 0; i < iterations; i++) x = x * 31 + i;
            sink = x;
        };
    }

    /** Waits and uses almost no CPU, like a call to a database. */
    public static Runnable sleep(long millis) {
        return () -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
