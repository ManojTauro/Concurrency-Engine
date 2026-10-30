package dev.conc.p9;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Challenge 1. Run an action, retrying failures with exponential backoff.
 *
 *  - at most policy.maxAttempts() attempts; below 1 throws IllegalArgumentException;
 *  - after failed attempt k (k = 1, 2, ...) and only if another attempt follows, wait
 *    cap_k = min(maxDelay, baseDelay * 2^(k-1)) through the sleeper. With Jitter.FULL the wait is
 *    Duration.ofNanos(random.nextLong(cap_k.toNanos() + 1)) instead (one nextLong call per wait).
 *    No wait after the last attempt. The doubling must never overflow, however large k gets;
 *  - a PermanentFailureException is thrown at once, no wait, no retry;
 *  - an InterruptedException (from the action or the sleeper) is thrown at once, no retry;
 *  - when every attempt failed, throw RetriesExhaustedException(maxAttempts, last exception).
 */
public final class Retry {

    private Retry() {
    }

    public static <T> T call(Callable<T> action, RetryPolicy policy, Sleeper sleeper, Random random) throws Exception {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
