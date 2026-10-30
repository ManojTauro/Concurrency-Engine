package dev.conc.p9;

public enum Jitter {
    NONE,
    /** Wait a random time between zero and the backoff delay. */
    FULL
}
