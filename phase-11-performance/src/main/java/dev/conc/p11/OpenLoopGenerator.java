package dev.conc.p11;

import java.time.Duration;

/**
 * Challenge 2, part 2. Requests arrive on a fixed schedule, whether or not earlier ones have
 * finished (real users do not wait for each other). Request i (i = 0, 1, 2, ...) is INTENDED at
 * start + i / ratePerSecond. A thread named "p11-sender" releases each request at its intended
 * time (sleep or park until then; never faster than the schedule) to a pool of {@code maxThreads}
 * threads named "p11-open-0", ... with an unbounded queue. The recorded latency of a request is
 * (time it completed) - (its INTENDED time), not (time it completed) - (time it actually started).
 *
 * Requests are issued while their intended time is before the end of the duration. run() waits for
 * all of them to complete and returns how many were issued. A call that throws is still recorded.
 * No "p11-" thread is alive afterwards. ratePerSecond <= 0 or maxThreads < 1 throws
 * IllegalArgumentException.
 */
public final class OpenLoopGenerator {

    private OpenLoopGenerator() {
    }

    public static long run(Service service, double ratePerSecond, Duration duration, int maxThreads, Recorder recorder) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
