package dev.conc.p5;

/**
 * Challenge 3. One lock protects everything. Use the synchronized keyword. Decide: what does your lock actually lock?
 * Must pass the same ProfileStoreContract tests as the other stores.
 */
public final class SynchronizedProfileStore implements ProfileStore {

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
