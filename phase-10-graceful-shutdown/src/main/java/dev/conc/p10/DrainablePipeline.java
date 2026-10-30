package dev.conc.p10;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Challenge 3. A pipeline you can stop without losing accepted work.
 *
 *  - workers threads named "p10-worker-0", "p10-worker-1", ... take events from a bounded queue of
 *    the given capacity and call the sink; a sink that throws does not stop the worker;
 *  - submit() returns true when the event was accepted (it waits while the queue is full) and
 *    false when the pipeline is closing or closed; the event is then NOT accepted;
 *  - EVERY event for which submit() returned true reaches the sink before close() reports success,
 *    even if submit() and close() run at the same moment on different threads;
 *  - close(timeout) stops accepting, lets the workers finish what is queued, ends the workers and
 *    returns DrainReport(accepted, processed, true). If that takes longer than the timeout it
 *    interrupts the workers and returns drained = false (processed may then be below accepted);
 *  - close() is idempotent and safe from several threads at once: every call returns the report of
 *    the first one;
 *  - shutdownHook(timeout) returns an UNSTARTED thread that calls close(timeout), to register with
 *    Runtime.getRuntime().addShutdownHook(...).
 * workers and capacity below 1 throw IllegalArgumentException.
 */
public final class DrainablePipeline {

    public DrainablePipeline(int workers, int capacity, Consumer<Event> sink) {
        // TODO
    }

    public boolean submit(Event event) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public DrainReport close(Duration timeout) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Thread shutdownHook(Duration timeout) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
