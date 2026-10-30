package dev.conc.p12;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Challenge 1. Run many blocking tasks at once, each on its own VIRTUAL thread (Executors.
 * newVirtualThreadPerTaskExecutor, closed with try-with-resources). Return the outcomes in the order
 * of the tasks. A task that throws gives Outcome(null, the exception it threw) (not an
 * ExecutionException); the others are not affected. Returns only after every task has finished.
 */
public final class FanOut {

    private FanOut() {
    }

    public static <T> List<Outcome<T>> runAll(List<Callable<T>> tasks) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
