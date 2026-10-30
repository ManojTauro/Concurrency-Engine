package dev.conc.p2;

import java.util.List;
import java.util.function.Consumer;

/**
 * Challenge 3. One producer thread, one consumer thread, joined by a java.util.concurrent
 * ArrayBlockingQueue of the given capacity.
 *
 * Contract of run():
 *  - the producer thread is named "p2-producer" and the consumer thread "p2-consumer";
 *  - the sink is called only from the consumer thread, once per event, in source order;
 *  - run() returns after every event reached the sink, and no "p2-" thread is alive any more;
 *  - an empty source returns consumed = 0;
 *  - if the sink throws, run() throws PipelineFailedException with that exception as the cause,
 *    promptly, and again leaves no "p2-" thread alive (the producer may be blocked on a full queue).
 */
public final class EventPipeline {

    private EventPipeline() {
    }

    public static PipelineResult run(List<Event> source, int capacity, Consumer<Event> sink) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
