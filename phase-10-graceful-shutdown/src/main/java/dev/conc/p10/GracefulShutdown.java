package dev.conc.p10;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * Challenge 1. The standard two-phase shutdown of an executor.
 *
 *  1. pool.shutdown() (no new tasks, queued ones still run) and wait up to {@code grace} for the
 *     pool to terminate. If it does: ShutdownReport(true, true, empty list).
 *  2. Otherwise pool.shutdownNow(): the list it returns is "neverStarted", running tasks are
 *     interrupted. Wait ONE more second (fixed) for the pool to terminate, then return
 *     ShutdownReport(false, pool.isTerminated(), neverStarted). A task that ignores its interrupt
 *     must not make close() wait longer than that one second.
 *  3. If the CALLING thread is interrupted while waiting: go to shutdownNow immediately, set the
 *     calling thread's interrupt flag again (do not swallow it), and return
 *     ShutdownReport(false, pool.isTerminated(), neverStarted) without further waiting.
 * Calling it on a pool that is already shut down is fine.
 */
public final class GracefulShutdown {

    private GracefulShutdown() {
    }

    public static ShutdownReport close(ExecutorService pool, Duration grace) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
