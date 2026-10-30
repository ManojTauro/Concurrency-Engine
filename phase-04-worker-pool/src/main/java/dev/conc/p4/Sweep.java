package dev.conc.p4;

import java.util.List;

/**
 * Challenge 3, part 2: measure throughput for several pool sizes. For each size in workerCounts
 * (in the given order) create a fixed pool of that many threads named "p4-sweep-1", ...,
 * submit {@code tasks} copies of the task, wait until all are done, shut the pool down, and report
 * tasks per second. No sweep thread may be alive when run() returns. tasks below 1 throws
 * IllegalArgumentException.
 */
public final class Sweep {

    public record Point(int workers, double tasksPerSecond) {
    }

    private Sweep() {
    }

    public static List<Point> run(Runnable task, int[] workerCounts, int tasks) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
