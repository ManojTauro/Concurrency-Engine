package dev.conc.p8;

public enum Outcome {
    ACCEPTED,
    /** The new event is in, but the oldest queued event was thrown away to make room. */
    ACCEPTED_DROPPED_OLDEST,
    /** The new event was refused and is gone. */
    REJECTED
}
