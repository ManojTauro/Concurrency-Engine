package dev.conc.p5;

import java.util.function.Supplier;

/**
 * Given, and deliberately NAIVE: wall-clock timing in one JVM run, no warmup control, no forks.
 * Run it, write down the numbers, then do not trust them. Redo the comparison with JMH when you
 * reach Phase 11 and see how much the answer changes. That difference is the lesson.
 *
 * Run: java -cp target/classes dev.conc.p5.ContentionMain
 */
public final class ContentionMain {

    public static void main(String[] args) throws Exception {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        int eventsPerThread = 500_000;
        System.out.println("threads=" + threads + ", events/thread=" + eventsPerThread);
        time("synchronized", SynchronizedProfileStore::new, threads, eventsPerThread);
        time("concurrent map", ConcurrentProfileStore::new, threads, eventsPerThread);
        time("striped locks (16)", () -> new LockedProfileStore(16), threads, eventsPerThread);
    }

    private static void time(String name, Supplier<ProfileStore> factory, int threads, int perThread) throws Exception {
        ProfileStore store = factory.get();
        Thread[] ts = new Thread[threads];
        long t0 = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int id = t;
            ts[t] = new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    store.record(new Event(i + 1, "user-" + ((id * 31 + i) % 64), "product-" + (i % 50),
                            EventType.values()[i % 4], i, i + 1));
                }
            });
            ts[t].start();
        }
        for (Thread t : ts) t.join();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("%-20s %6d ms  total=%d%n", name, ms, store.totalEvents());
    }
}
