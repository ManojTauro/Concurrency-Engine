package dev.conc.p4;

public enum Rejection {
    /** Throw RejectedExecutionException. */
    ABORT,
    /** Run the task on the thread that submitted it. */
    CALLER_RUNS,
    /** Drop the task that has waited longest in the queue and retry the new one. */
    DISCARD_OLDEST
}
