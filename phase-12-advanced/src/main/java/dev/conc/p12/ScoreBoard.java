package dev.conc.p12;

/**
 * Challenge 3. Two fields that change together and are read very often, protected with a
 * java.util.concurrent.locks.StampedLock.
 *
 *  - set(x) stores a = x and b = 2 * x under the WRITE lock;
 *  - read() returns a Snapshot that is never torn: its b is always exactly 2 * its a (initially
 *    both are 0). Try an OPTIMISTIC read first (tryOptimisticRead, copy the fields to locals, then
 *    validate). Only if validation fails, fall back to a real READ lock;
 *  - optimisticReads() counts the reads that were served by a validated optimistic stamp, and
 *    fallbackReads() the reads that had to take the read lock. Their sum is the number of reads;
 *  - the fields are plain (non-volatile) fields.
 * StampedLock is not reentrant: never call set() or read() while holding the lock.
 */
public final class ScoreBoard {

    public ScoreBoard() {
        // TODO
    }

    public void set(long x) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Snapshot read() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long optimisticReads() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long fallbackReads() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
