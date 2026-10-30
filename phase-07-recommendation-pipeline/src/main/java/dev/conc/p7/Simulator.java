package dev.conc.p7;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Given. Stands in for the network: a future completes after a delay WITHOUT blocking any caller.
 * It is one thread named "p7-sim", and any callback you attach with a non-async method (thenApply,
 * thenCompose, ...) runs ON that thread when the future completes. Keep that in mind in challenge 4.
 */
public final class Simulator {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "p7-sim");
        t.setDaemon(true);
        return t;
    });

    private Simulator() {
    }

    /** A future that completes with value.get() after the delay; if the supplier throws, it fails. */
    public static <T> CompletableFuture<T> after(long millis, Supplier<T> value) {
        CompletableFuture<T> f = new CompletableFuture<>();
        SCHEDULER.schedule(() -> {
            try {
                f.complete(value.get());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        }, millis, TimeUnit.MILLISECONDS);
        return f;
    }

    /** Run something on the simulator thread after the delay (a timer). */
    public static void schedule(long millis, Runnable task) {
        SCHEDULER.schedule(task, millis, TimeUnit.MILLISECONDS);
    }
}
