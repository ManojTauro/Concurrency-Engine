package dev.conc.p12;

import java.util.concurrent.CompletableFuture;

/**
 * Challenge 5. A scheduler for more runnable splits than threads, in the style of Trino's
 * TaskExecutor: a FIXED number of threads run SplitRunners one slice at a time.
 *
 *  - {@code threads} threads named "p12-split-0", "p12-split-1", ...; one FIFO queue of runnable splits;
 *  - a worker takes the split at the head, calls process(quantumNanos) once, then:
 *      Finished -> complete the split's future normally;
 *      Yielded  -> put the split at the TAIL of the queue (everyone else gets a turn);
 *      Blocked  -> do NOT keep a thread on it: when its signal completes (normally or not), put the
 *                  split at the tail of the queue;
 *      an exception from process -> complete the split's future exceptionally with that exception
 *                  and drop the split; other splits are not affected;
 *  - enqueue(split) returns a future that completes when the split is Finished (or failed);
 *  - close() stops the threads (interrupt and join) and cancels the futures still pending;
 *    after close() no "p12-split-" thread is alive.
 */
public final class TimeSlicedExecutor implements AutoCloseable {

    public TimeSlicedExecutor(int threads, long quantumNanos) {
        // TODO
    }

    public CompletableFuture<Void> enqueue(SplitRunner split) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
