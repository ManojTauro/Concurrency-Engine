package dev.conc.p12;

/**
 * Challenge 6. Trino's multilevel feedback queue, simplified. Work that has used little time is
 * served first, but long jobs are not starved.
 *
 * The queue has one FIFO queue per level. thresholds[i] is the scheduled time at which an entry
 * moves up to level i (thresholds[0] must be 0 and the array strictly ascending, otherwise
 * IllegalArgumentException). Not thread-safe: the caller synchronises.
 *
 *  - add(item): a new entry with scheduledNanos 0, put at the tail of level 0, and returned;
 *  - poll(): remove and return an entry (null if the queue is empty): among the NON-EMPTY levels
 *    choose the level i with the smallest consumed(i) * 2^i (ties: the lower level), and take the
 *    head of that level's FIFO. Level i is entitled to half the time of level i-1;
 *  - charge(entry, nanos): the entry used nanos of CPU. Add nanos to consumed(entry.level()) for the
 *    level it was at BEFORE the charge, then add nanos to the entry's own scheduledNanos;
 *  - requeue(entry): put the entry at the tail of the level that its scheduledNanos now gives it;
 *  - consumed(level): total nanos charged to entries while they were at that level.
 */
public final class MultilevelQueue<T> {

    public MultilevelQueue(long[] thresholdsNanos) {
        // TODO
    }

    public LevelEntry<T> add(T item) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public LevelEntry<T> poll() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void charge(LevelEntry<T> entry, long nanos) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void requeue(LevelEntry<T> entry) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long consumed(int level) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
