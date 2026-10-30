package dev.conc.p8;

public enum ShedOutcome {
    ACCEPTED,
    /** Accepted after evicting a queued event of lower priority. */
    ACCEPTED_EVICTED,
    /** Refused: nothing queued has lower priority than this event. */
    REJECTED
}
