package dev.conc.p12;

import java.util.concurrent.CompletableFuture;

/** What one time slice of a SplitRunner ended with. */
public sealed interface SplitResult {
    /** The work is complete. */
    record Finished() implements SplitResult {
    }

    /** The quantum is used up; there is more to do, run me again later. */
    record Yielded() implements SplitResult {
    }

    /** Nothing to do until the signal completes (waiting for data, I/O, a downstream buffer). */
    record Blocked(CompletableFuture<?> signal) implements SplitResult {
    }
}
