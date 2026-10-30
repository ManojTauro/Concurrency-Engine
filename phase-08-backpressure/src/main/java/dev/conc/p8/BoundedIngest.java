package dev.conc.p8;

import java.time.Duration;

/**
 * Challenge 2. A bounded ingest queue with a policy for what happens when it is full. FIFO.
 * The queue never holds more than {@code capacity} events, whatever the threads do.
 *
 *  - BLOCK: submit waits for space (interruptible) and returns ACCEPTED;
 *  - REJECT_NEW: when full submit returns REJECTED at once and counts it in rejected();
 *  - DROP_OLDEST: when full submit removes the oldest event, counts it in droppedOldest(), adds
 *    the new one and returns ACCEPTED_DROPPED_OLDEST; it never waits;
 *  - TIMEOUT: when full submit waits up to {@code timeout} for space; if none appears it returns
 *    REJECTED (and counts it in rejected());
 *  - when there is room, every policy returns ACCEPTED;
 *  - capacity below 1 throws IllegalArgumentException.
 */
public final class BoundedIngest {

    public BoundedIngest(int capacity, Policy policy, Duration timeout) {
        // TODO
    }

    public Outcome submit(Event event) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** The oldest event, waiting if there is none. */
    public Event take() throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long rejected() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long droppedOldest() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
