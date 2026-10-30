package dev.conc.p5;

import java.util.List;

/**
 * Challenge 1. Show lost updates with plain threads (Thread.start / join, no executors yet).
 * Worker exceptions are part of the finding; do not let them hide a run.
 */
public final class RaceHarness {

    /**
     * Start {@code threads} threads. Each calls {@link UnsafeCounter#increment()}
     * {@code incrementsPerThread} times on ONE shared counter. Join them all.
     *
     * @return expected = threads * incrementsPerThread, actual = counter.get()
     */
    public static RaceResult countWithoutSync(int threads, int incrementsPerThread) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Split {@code events} into {@code threads} slices (any split that covers every event once).
     * Each thread records its slice into ONE shared {@link UnsafeProfileStore}. Join them all.
     *
     * @return expected = events.size(), actual = store.totalEvents()
     */
    public static RaceResult recordWithoutSync(int threads, List<Event> events) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
