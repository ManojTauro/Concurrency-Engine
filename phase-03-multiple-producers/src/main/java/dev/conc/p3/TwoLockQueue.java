package dev.conc.p3;

/**
 * Challenge 3. An unbounded FIFO linked queue with TWO locks: producers (offer) only contend with
 * each other on the tail lock, consumers (poll) only with each other on the head lock. This is the
 * two-lock queue of Michael and Scott (1996).
 *
 *  - offer(null) throws NullPointerException; poll() returns null when empty (never waits);
 *  - size() is exact once all operations have finished, and never negative while they run;
 *  - an offer and a poll must be able to run at the same time, even on a queue with 0 or 1 items.
 * Decide: what is the first node, and why does the queue need it?
 */
public final class TwoLockQueue<T> {

    public TwoLockQueue() {
        // TODO
    }

    public void offer(T item) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public T poll() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
