package dev.conc.p4;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Challenge 1. A fixed-size worker pool built from threads and a bounded queue. Do not use
 * ExecutorService or ThreadPoolExecutor; you may use a BlockingQueue.
 *
 *  - {@code workers} threads named "p4-simple-0", "p4-simple-1", ... start in the constructor;
 *  - submit() queues the task; if the queue is full or the pool is shut down it throws
 *    RejectedExecutionException and the task never runs;
 *  - a task that throws must not kill its worker;
 *  - shutdown() stops accepting new tasks; tasks already queued still run; it does not wait;
 *  - awaitTermination() returns true once every task has run and every worker thread has ended,
 *    false if the time ran out first; calling shutdown() twice is harmless.
 */
public final class SimplePool {

    public SimplePool(int workers, int queueCapacity) {
        // TODO
    }

    public void submit(Runnable task) throws RejectedExecutionException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void shutdown() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
