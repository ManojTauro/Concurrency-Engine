package dev.conc.p8;

import java.time.Duration;

/**
 * Challenge 1. Break it on purpose: a producer faster than its consumer, with NOTHING limiting the
 * queue. Use a java.util.concurrent.LinkedBlockingQueue with no capacity.
 *
 *  - a producer thread "p8-producer" adds events at producerPerSecond (a value of 0 or less means
 *    as fast as it can: only use that with a small heap, it fills memory);
 *  - a consumer thread "p8-consumer" takes events at no more than consumerPerSecond (it may idle
 *    when the queue is empty, but it must not catch up faster than that rate);
 *  - the calling thread records queue.size() every 50 ms for the whole duration;
 *  - then both threads are stopped and joined: no "p8-" thread is alive when run() returns;
 *  - produced and consumed count the events made and taken.
 */
public final class FloodExperiment {

    private FloodExperiment() {
    }

    public static FloodResult run(Duration duration, long producerPerSecond, long consumerPerSecond) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
