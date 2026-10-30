package dev.conc.p11;

import java.time.Duration;

/**
 * Challenge 2, part 1. The usual load test: {@code clients} threads, each sends a request, waits
 * for the answer, then sends the next. Each request's latency is (time the call returned) - (time
 * it was sent). Threads are named "p11-client-0", "p11-client-1", ...; run() stops sending after
 * the duration, waits for the clients to finish, and returns the number of requests made. A call
 * that throws is still timed and recorded. No "p11-" thread is alive afterwards.
 */
public final class ClosedLoopGenerator {

    private ClosedLoopGenerator() {
    }

    public static long run(Service service, int clients, Duration duration, Recorder recorder) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
