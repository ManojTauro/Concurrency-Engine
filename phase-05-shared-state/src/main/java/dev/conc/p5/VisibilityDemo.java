package dev.conc.p5;

import java.time.Duration;

/**
 * Challenge 2. Does a thread see another thread's write?
 *
 * Both methods work the same way:
 *  1. start a DAEMON worker thread that spins in an empty loop until a flag becomes true
 *  2. the caller sleeps for {@code runFor}, then sets the flag
 *  3. wait up to {@code giveUp} for the worker to finish
 *  4. return true if the worker finished, false if it is still running
 *
 * The only difference between the two methods: the flag is a plain boolean in one and volatile in
 * the other. Keep the worker's loop empty ("while (!stop) { }"); do not call anything inside it.
 */
public final class VisibilityDemo {

    public static boolean plainFlagStops(Duration runFor, Duration giveUp) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public static boolean volatileFlagStops(Duration runFor, Duration giveUp) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
