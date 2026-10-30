package dev.conc.p11;

/** Given. Two kinds of task to measure a pool with. */
public final class Work {
    private Work() {
    }

    private static volatile long sink;

    /** CPU only, never waits. */
    public static Runnable cpuBurn(int iterations) {
        return () -> {
            long x = 1;
            for (int i = 0; i < iterations; i++) x = x * 31 + i;
            sink = x;
        };
    }

    /** Waits, almost no CPU: a stand-in for a database call. */
    public static Runnable sleepMillis(long millis) {
        return () -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}
