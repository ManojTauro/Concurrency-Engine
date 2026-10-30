package dev.conc.p6;

/**
 * Challenge 3. The single-writer idea: every user belongs to one lane, and only that lane's thread
 * ever touches the user's UserBehaviourProfile. There is no lock and no concurrent map around the
 * profiles; the ordering is the synchronisation. Use your LaneExecutor from challenge 1.
 *
 *  - record() hands the event to the user's lane; before applying it, the lane compares
 *    event.userSeq() with profile.lastUserSeq() and counts a violation if it is not larger;
 *  - count() and score() read through the same lane (submit the read and wait for its answer), so
 *    they never see a half-applied event; an unknown user gives 0;
 *  - awaitIdle() returns when everything recorded before the call has been applied;
 *  - totalEvents() and orderViolations() are exact once idle;
 *  - close() shuts the lanes down after the queued work; record() after close() throws
 *    java.util.concurrent.RejectedExecutionException.
 */
public final class LaneProfileStore implements OrderedStore {

    public LaneProfileStore(int lanes) {
        // TODO
    }

    @Override
    public void record(Event event) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void awaitIdle() throws InterruptedException {
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

    @Override
    public long orderViolations() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
