package dev.conc.p8;

import java.time.Duration;

/**
 * Challenge 3, part 1. A gate that lets at most {@code credits} units of work be in flight.
 * acquire() takes a credit, waiting if none is free; release() gives one back.
 *
 *  - tryAcquire waits up to the timeout and returns false if no credit appeared;
 *  - inFlight() is credits taken minus credits returned;
 *  - release() when nothing is in flight throws IllegalStateException (a double release would let
 *    more work in than the limit);
 *  - credits below 1 throws IllegalArgumentException.
 */
public final class CreditGate {

    public CreditGate(int credits) {
        // TODO
    }

    public void acquire() throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean tryAcquire(Duration timeout) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void release() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int inFlight() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
