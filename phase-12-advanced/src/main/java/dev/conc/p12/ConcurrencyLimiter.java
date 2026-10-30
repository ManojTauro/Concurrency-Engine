package dev.conc.p12;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Challenge 4, part 1. At most {@code permits} calls run inside the limiter at the same time (use
 * a Semaphore). call() waits for a permit, runs the action, and ALWAYS gives the permit back, even
 * if the action throws (the exception propagates unchanged). tryCall() waits at most the timeout
 * for a permit and returns Optional.empty() if none became free; otherwise it runs the action
 * like call() and returns its result (an action that returns null gives Optional.empty() too).
 * inUse() is the number of permits currently taken. permits below 1 throws IllegalArgumentException.
 */
public final class ConcurrencyLimiter {

    public ConcurrencyLimiter(int permits) {
        // TODO
    }

    public <T> T call(Callable<T> action) throws Exception {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public <T> Optional<T> tryCall(Callable<T> action, Duration timeout) throws Exception {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int inUse() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
