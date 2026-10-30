package dev.conc.p11;

import java.util.List;

/**
 * Challenge 3. The experiment of the phase: how do throughput and latency change with the number
 * of workers?
 *
 * run(): for each count in workerCounts (in order), create a fixed pool of that many threads named
 * "p11-sweep-1", ... with an unbounded queue, then at one instant submit {@code events} copies of
 * the work, all together. Each task records its sojourn time (completion minus the moment it was
 * submitted) in a ListRecorder. throughput = events / (time from the first submission until the
 * last task completed). Return SweepPoint(workers, throughput, p50, p99) per count. No "p11-"
 * thread is alive afterwards. events below 1 throws IllegalArgumentException.
 *
 * toCsv(): "workers,throughput_per_s,p50_ms,p99_ms" as the first line, then one line per point,
 * all joined with "\n" and no trailing newline. Use Locale.ROOT; throughput with 1 decimal, the two
 * latencies in milliseconds with 3 decimals, for example "8,1523.4,2.500,31.250".
 */
public final class WorkerSweep {

    private WorkerSweep() {
    }

    public static List<SweepPoint> run(Runnable work, int[] workerCounts, int events) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public static String toCsv(List<SweepPoint> points) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
