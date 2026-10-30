package dev.conc.p12;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Given, for the comparison: the same job on a fixed pool of platform threads. */
public final class PlatformFanOut {

    private PlatformFanOut() {
    }

    public static <T> List<Outcome<T>> runAll(List<Callable<T>> tasks, int poolSize) {
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> t : tasks) futures.add(pool.submit(t));
            List<Outcome<T>> out = new ArrayList<>();
            for (Future<T> f : futures) {
                try {
                    out.add(new Outcome<>(f.get(), null));
                } catch (ExecutionException e) {
                    out.add(new Outcome<>(null, e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    out.add(new Outcome<>(null, e));
                }
            }
            return out;
        }
    }
}
