package dev.conc.p10;

/**
 * @param accepted  events for which submit() returned true
 * @param processed events the sink was called for (it returned or threw)
 * @param drained   true if close() finished in time and accepted == processed
 */
public record DrainReport(long accepted, long processed, boolean drained) {
}
