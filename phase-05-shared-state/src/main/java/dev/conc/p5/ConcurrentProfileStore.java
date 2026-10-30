package dev.conc.p5;

/**
 * Challenge 4. No explicit lock. Use ConcurrentHashMap's per-key atomic operations, and LongAdder (or AtomicLong) for the total. Do not use the synchronized keyword.
 * Must pass the same ProfileStoreContract tests as the other stores.
 */
public final class ConcurrentProfileStore implements ProfileStore {

    @Override
    public void record(Event event) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long count(String userId, EventType type) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long score(String userId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public long totalEvents() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
