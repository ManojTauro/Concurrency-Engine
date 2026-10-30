package dev.conc.p10;

import java.util.List;

/**
 * @param clean        every task finished inside the grace period, nothing was interrupted
 * @param terminated   the pool had no running thread left when close() returned
 * @param neverStarted the tasks that were still queued and never ran
 */
public record ShutdownReport(boolean clean, boolean terminated, List<Runnable> neverStarted) {
}
