package dev.conc.p3;

import java.util.List;
import java.util.function.Consumer;

/**
 * Challenge 1. One producer thread per source list, one consumer thread, one ArrayBlockingQueue.
 *
 * Contract of run():
 *  - producer threads are named "p3-producer-0", "p3-producer-1", ...; the consumer is "p3-consumer";
 *  - the sink runs only on the consumer thread;
 *  - events of one source reach the sink in that source's order (events of different sources interleave);
 *  - run() returns the number of events consumed once every source is fully delivered, and no "p3-"
 *    thread is alive any more; no sources, or only empty sources, return 0;
 *  - the consumer must stop exactly when the LAST producer is done, whichever producer that is.
 */
public final class MultiProducerPipeline {

    private MultiProducerPipeline() {
    }

    public static long run(List<List<Event>> sources, int capacity, Consumer<Event> sink) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
