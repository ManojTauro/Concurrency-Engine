package dev.conc.p8;

/** What a full ingest queue does with a new event. */
public enum Policy {
    /** Wait until there is space. */
    BLOCK,
    /** Refuse the new event at once. */
    REJECT_NEW,
    /** Throw away the oldest queued event to make room, never wait. */
    DROP_OLDEST,
    /** Wait up to the configured timeout, then refuse. */
    TIMEOUT
}
