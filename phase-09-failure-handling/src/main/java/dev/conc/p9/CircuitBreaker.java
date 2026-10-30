package dev.conc.p9;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Challenge 3. Stop calling a service that keeps failing, and test it again after a pause.
 *
 *  - CLOSED: calls run. Any Exception from the action counts as a failure and is rethrown; a
 *    success resets the count of consecutive failures to 0. When consecutive failures reach
 *    failureThreshold the breaker becomes OPEN and remembers the time (clock.nanos());
 *  - OPEN: call() throws CircuitOpenException WITHOUT running the action, until openDuration has
 *    passed. Then the next call is the single trial call (state HALF_OPEN);
 *  - HALF_OPEN: exactly one trial call is in flight; every other call that arrives meanwhile throws
 *    CircuitOpenException. The trial's success makes the breaker CLOSED (count 0); its failure makes
 *    it OPEN again with a new open time, and the exception is rethrown;
 *  - state() reports CLOSED, OPEN, or HALF_OPEN; an OPEN breaker whose openDuration has passed
 *    reports HALF_OPEN, because the next call will be allowed through;
 *  - the action must run OUTSIDE any lock, and the class is safe to call from many threads;
 *  - failureThreshold below 1 throws IllegalArgumentException.
 */
public final class CircuitBreaker {

    public CircuitBreaker(int failureThreshold, Duration openDuration, Clock clock) {
        // TODO
    }

    public <T> T call(Callable<T> action) throws Exception {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public State state() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
