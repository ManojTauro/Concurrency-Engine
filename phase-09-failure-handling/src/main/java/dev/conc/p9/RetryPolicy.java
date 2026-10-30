package dev.conc.p9;

import java.time.Duration;

/**
 * @param maxAttempts total attempts including the first (at least 1)
 * @param baseDelay   the wait after the first failure
 * @param maxDelay    no wait is ever longer than this
 * @param jitter      NONE or FULL
 */
public record RetryPolicy(int maxAttempts, Duration baseDelay, Duration maxDelay, Jitter jitter) {
}
