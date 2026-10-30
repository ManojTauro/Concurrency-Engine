package dev.conc.p5;

/**
 * Challenge 5. Striped locking with ReentrantLock: the user ids are spread over {@code stripes}
 * separate maps, each guarded by its own lock, so users in different stripes do not wait on each
 * other. Reads must take the right lock too. totalEvents() must be correct without taking every lock.
 * Must pass the same ProfileStoreContract tests as the other stores.
 */
public final class LockedProfileStore implements ProfileStore {

    /** @throws IllegalArgumentException if stripes is not positive */
    public LockedProfileStore(int stripes) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

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
