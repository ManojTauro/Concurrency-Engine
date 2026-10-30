package dev.conc.p12;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/**
 * Challenge 2. Sum the event weights per user with fork/join: a RecursiveTask over a range of the
 * list. If the range has at most {@code threshold} events, compute it directly into a HashMap;
 * otherwise split it in half, fork one half, compute the other in the current thread, join, and
 * merge the two maps. The work MUST run in the given pool (pool.invoke), not the common pool, and
 * must not use parallel streams. An empty list gives an empty map; threshold below 1 throws
 * IllegalArgumentException.
 */
public final class ParallelScorer {

    private ParallelScorer() {
    }

    public static Map<String, Long> scoreByUser(List<Event> events, ForkJoinPool pool, int threshold) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
