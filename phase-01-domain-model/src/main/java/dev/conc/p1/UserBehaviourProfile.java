package dev.conc.p1;

/**
 * What the engine knows about one user. Deliberately NOT thread-safe: Phase 5 shares it between
 * threads and breaks it on purpose.
 */
public final class UserBehaviourProfile {

    public UserBehaviourProfile(String userId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public String userId() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Fold one event into the profile. */
    public void apply(Event event) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long count(EventType type) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Sum of the weights of all applied events. */
    public long score() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Number of different products seen. */
    public int distinctProducts() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Highest userSeq applied so far, 0 if none. */
    public long lastUserSeq() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Largest timestamp applied so far, -1 if none. */
    public long lastTimestampMillis() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
