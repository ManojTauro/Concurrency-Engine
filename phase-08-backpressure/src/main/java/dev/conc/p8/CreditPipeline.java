package dev.conc.p8;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * Challenge 3, part 2. Bound the work in flight end to end, not just the queue. The calling thread
 * is the producer: for each event it takes a credit from a CreditGate(credits) BEFORE handing the
 * event to the pool, and the worker gives the credit back when the sink is done, even if the sink
 * threw. The pool is given (it may have an unbounded queue: the credits are the only limit). So
 * queued plus running events never exceed {@code credits}.
 *
 * run() returns the number of events the sink handled WITHOUT throwing, after every event has been
 * handled. A throwing sink must not stall the run. The pool is not shut down by run().
 */
public final class CreditPipeline {

    private CreditPipeline() {
    }

    public static long run(List<Event> events, int credits, ThreadPoolExecutor pool, Consumer<Event> sink) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
