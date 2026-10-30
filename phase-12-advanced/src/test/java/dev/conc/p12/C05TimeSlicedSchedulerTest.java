package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class C05TimeSlicedSchedulerTest {
    private static final long MS = 1_000_000L;

    private static boolean splitThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p12-split-"));
    }

    @Test
    void shortSplitsOvertakeAnEarlierLongOneOnASingleThread() throws Exception {
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(1, 10 * MS)) {
            long t0 = System.nanoTime();
            List<String> order = Collections.synchronizedList(new ArrayList<>());
            CompletableFuture<Void> longJob = ex.enqueue(new BurnSplit(600 * MS)).whenComplete((v, e) -> order.add("long"));
            List<CompletableFuture<Void>> shorts = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int n = i;
                shorts.add(ex.enqueue(new BurnSplit(15 * MS)).whenComplete((v, e) -> order.add("short" + n)));
            }
            for (CompletableFuture<Void> s : shorts) s.get(10, TimeUnit.SECONDS);
            long shortsDone = (System.nanoTime() - t0) / 1_000_000;
            longJob.get(10, TimeUnit.SECONDS);
            assertEquals("long", order.get(order.size() - 1), "order: " + order);
            assertTrue(shortsDone < 450, "the short splits finished after " + shortsDone + " ms; run to completion would take 600 ms or more");
        }
    }

    @Test
    void aBlockedSplitDoesNotHoldAThread() throws Exception {
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(1, 10 * MS)) {
            CompletableFuture<Void> signal = new CompletableFuture<>();
            int[] slices = {0};
            CompletableFuture<Void> blocked = ex.enqueue(q -> {
                slices[0]++;
                return slices[0] == 1 ? new SplitResult.Blocked(signal) : new SplitResult.Finished();
            });
            CompletableFuture<Void> other = ex.enqueue(new BurnSplit(5 * MS));
            other.get(5, TimeUnit.SECONDS);
            assertFalse(blocked.isDone(), "the blocked split must wait for its signal");
            assertEquals(1, slices[0], "and must not be polled while it is blocked");
            signal.complete(null);
            blocked.get(5, TimeUnit.SECONDS);
            assertEquals(2, slices[0]);
        }
    }

    @Test
    void aBlockedSplitWhoseSignalFailsIsStillRunAgain() throws Exception {
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(1, 10 * MS)) {
            CompletableFuture<Void> signal = new CompletableFuture<>();
            int[] slices = {0};
            CompletableFuture<Void> job = ex.enqueue(q -> ++slices[0] == 1 ? new SplitResult.Blocked(signal) : new SplitResult.Finished());
            signal.completeExceptionally(new IllegalStateException("source failed"));
            job.get(5, TimeUnit.SECONDS);
            assertEquals(2, slices[0]);
        }
    }

    @Test
    void aFailingSplitFailsOnlyItsOwnFuture() throws Exception {
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(2, 10 * MS)) {
            IllegalStateException boom = new IllegalStateException("boom");
            CompletableFuture<Void> bad = ex.enqueue(q -> {
                throw boom;
            });
            CompletableFuture<Void> good = ex.enqueue(new BurnSplit(20 * MS));
            ExecutionException ee = assertThrows(ExecutionException.class, () -> bad.get(5, TimeUnit.SECONDS));
            assertSame(boom, ee.getCause());
            good.get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void theQuantumIsPassedToEverySlice() throws Exception {
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(1, 7 * MS)) {
            List<Long> seen = Collections.synchronizedList(new ArrayList<>());
            int[] n = {0};
            ex.enqueue(q -> {
                seen.add(q);
                return ++n[0] < 3 ? new SplitResult.Yielded() : new SplitResult.Finished();
            }).get(5, TimeUnit.SECONDS);
            assertEquals(List.of(7 * MS, 7 * MS, 7 * MS), seen);
        }
    }

    @Test
    void manySplitsRunOnAFixedNumberOfNamedThreads() throws Exception {
        Set<String> threads = Collections.synchronizedSet(new HashSet<>());
        AtomicLong slices = new AtomicLong();
        try (TimeSlicedExecutor ex = new TimeSlicedExecutor(3, 2 * MS)) {
            List<CompletableFuture<Void>> fs = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                BurnSplit inner = new BurnSplit(6 * MS);
                fs.add(ex.enqueue(q -> {
                    threads.add(Thread.currentThread().getName());
                    slices.incrementAndGet();
                    return inner.process(q);
                }));
            }
            for (CompletableFuture<Void> f : fs) f.get(30, TimeUnit.SECONDS);
        }
        assertTrue(threads.size() >= 2 && threads.size() <= 3, threads.toString());
        threads.forEach(t -> assertTrue(t.startsWith("p12-split-"), t));
        assertEquals(300, slices.get(), "100 splits of 3 slices each");
    }

    @Test
    void closeEndsTheThreads() throws Exception {
        TimeSlicedExecutor ex = new TimeSlicedExecutor(2, MS);
        ex.enqueue(new BurnSplit(5 * MS)).get(5, TimeUnit.SECONDS);
        ex.close();
        assertFalse(splitThreadAlive());
    }

    @Test
    void closeCancelsWhatIsStillPending() throws Exception {
        TimeSlicedExecutor ex = new TimeSlicedExecutor(1, 10 * MS);
        CompletableFuture<Void> never = ex.enqueue(q -> new SplitResult.Blocked(new CompletableFuture<>()));
        Thread.sleep(100);
        ex.close();
        assertTrue(never.isCancelled() || never.isCompletedExceptionally());
        assertFalse(splitThreadAlive());
    }
}
