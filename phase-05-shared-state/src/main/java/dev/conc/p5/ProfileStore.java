package dev.conc.p5;

/** Shared state: many worker threads call record(), other threads read. */
public interface ProfileStore {

    /** Fold the event into its user's profile, creating the profile on first sight. */
    void record(Event event);

    /** Number of events of this type recorded for this user, 0 for an unknown user. */
    long count(String userId, EventType type);

    /** Score of this user, 0 for an unknown user. */
    long score(String userId);

    /** Total number of events recorded across all users. */
    long totalEvents();
}
