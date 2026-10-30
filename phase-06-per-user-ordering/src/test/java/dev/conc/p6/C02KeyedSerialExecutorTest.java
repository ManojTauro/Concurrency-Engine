package dev.conc.p6;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C02KeyedSerialExecutorTest {

    private static ExecutorService pool(int threads) {
        AtomicInteger n = new AtomicInteger();
        return Executors.newFixedThreadPool(threads, r -> new Thread(r, "p6-shared-" + n.incrementAndGet()));
    }


    /** The keyed executor re-submits itself to the pool, so the pool must outlive all keyed work. */
    private static void awaitNoActiveKeys(KeyedSerialExecutor ex) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (ex.activeKeys() > 0 && System.nanoTime() < deadline) Thread.sleep(5);
    }

    @Test
    void perKeyOrderHoldsAndNeverOverlaps() throws Exception {
        ExecutorService pool = pool(4);
        KeyedSerialExecutor ex = new KeyedSerialExecutor(pool, 8);
        int keys = 2_000, perKey = 20, submitters = 3;
        ConcurrentHashMap<String, List<Integer>> seen = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AtomicInteger> inFlight = new ConcurrentHashMap<>();
        AtomicInteger overlaps = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int s = 0; s < submitters; s++) {
            int id = s;
            ts.add(new Thread(() -> {
                for (int i = 0; i < perKey; i++) {
                    for (int k = id; k < keys; k += submitters) {
                        String key = "key-" + k;
                        int n = i;
                        ex.execute(key, () -> {
                            AtomicInteger f = inFlight.computeIfAbsent(key, x -> new AtomicInteger());
                            if (f.incrementAndGet() > 1) overlaps.incrementAndGet();
                            seen.computeIfAbsent(key, x -> new ArrayList<>()).add(n);
                            f.decrementAndGet();
                        });
                    }
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        awaitNoActiveKeys(ex);
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertEquals(0, overlaps.get(), "two tasks of one key ran at the same time");
        assertEquals(keys, seen.size());
        seen.forEach((key, list) -> {
            assertEquals(perKey, list.size(), key);
            for (int i = 0; i < perKey; i++) assertEquals(i, list.get(i), key);
        });
        assertEquals(0, ex.activeKeys());
    }

    @Test
    void usesOnlyThePoolsThreadsEvenWithManyKeys() throws Exception {
        ExecutorService pool = pool(4);
        KeyedSerialExecutor ex = new KeyedSerialExecutor(pool, 8);
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        for (int k = 0; k < 10_000; k++) ex.execute("key-" + k, () -> names.add(Thread.currentThread().getName()));
        awaitNoActiveKeys(ex);
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(names.size() <= 4, names.toString());
        assertEquals(0, ex.activeKeys());
    }

    @Test
    void aThrowingTaskDoesNotBlockItsKey() throws Exception {
        ExecutorService pool = pool(2);
        KeyedSerialExecutor ex = new KeyedSerialExecutor(pool, 4);
        ex.execute("k", () -> {
            throw new IllegalStateException("boom");
        });
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < 10; i++) ex.execute("k", ran::incrementAndGet);
        awaitNoActiveKeys(ex);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(10, ran.get());
        assertEquals(0, ex.activeKeys());
    }

    @Test
    void aHugeBacklogOnOneKeyDoesNotStarveAnotherKey() throws Exception {
        ExecutorService pool = pool(1);
        KeyedSerialExecutor ex = new KeyedSerialExecutor(pool, 16);
        AtomicInteger hotDone = new AtomicInteger();
        AtomicInteger hotDoneWhenOtherRan = new AtomicInteger(-1);
        for (int i = 0; i < 300; i++) ex.execute("hot", () -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            hotDone.incrementAndGet();
        });
        ex.execute("other", () -> hotDoneWhenOtherRan.set(hotDone.get()));
        awaitNoActiveKeys(ex);
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertEquals(300, hotDone.get());
        int when = hotDoneWhenOtherRan.get();
        assertTrue(when >= 0 && when < 100, "the other key ran only after " + when + " of 300 hot tasks");
    }

    @Test
    void batchBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new KeyedSerialExecutor(Runnable::run, 0));
    }

    @Test
    void worksWithAnInlineExecutor() {
        KeyedSerialExecutor ex = new KeyedSerialExecutor(Runnable::run, 3);
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int n = i;
            ex.execute("k", () -> order.add(n));
        }
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), order);
        assertEquals(0, ex.activeKeys());
    }
}
