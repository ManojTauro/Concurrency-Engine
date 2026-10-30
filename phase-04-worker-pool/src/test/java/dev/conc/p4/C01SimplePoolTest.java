package dev.conc.p4;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C01SimplePoolTest {

    private static boolean simpleThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p4-simple-"));
    }

    @Test
    void runsEveryTask() throws Exception {
        SimplePool pool = new SimplePool(4, 2_000);
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < 1_000; i++) pool.submit(ran::incrementAndGet);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1_000, ran.get());
    }

    @Test
    void usesSeveralNamedWorkersButNoMoreThanConfigured() throws Exception {
        SimplePool pool = new SimplePool(4, 100);
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < 40; i++) {
            pool.submit(() -> {
                names.add(Thread.currentThread().getName());
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {
                }
            });
        }
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(names.size() > 1 && names.size() <= 4, "threads used: " + names);
        names.forEach(n -> assertTrue(n.startsWith("p4-simple-"), n));
    }

    @Test
    void aThrowingTaskDoesNotKillItsWorker() throws Exception {
        SimplePool pool = new SimplePool(2, 200);
        for (int i = 0; i < 20; i++) pool.submit(() -> {
            throw new IllegalStateException("boom");
        });
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < 50; i++) pool.submit(ran::incrementAndGet);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(50, ran.get());
    }

    @Test
    void shutdownStillRunsQueuedTasks() throws Exception {
        SimplePool pool = new SimplePool(2, 20);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(2);
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < 2; i++) pool.submit(() -> {
            started.countDown();
            try {
                gate.await();
            } catch (InterruptedException ignored) {
            }
            ran.incrementAndGet();
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        for (int i = 0; i < 10; i++) pool.submit(ran::incrementAndGet);
        pool.shutdown();
        assertFalse(pool.awaitTermination(100, TimeUnit.MILLISECONDS), "workers are still blocked");
        gate.countDown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(12, ran.get());
    }

    @Test
    void rejectsWhenTheQueueIsFull() throws Exception {
        SimplePool pool = new SimplePool(1, 2);
        CountDownLatch gate = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        pool.submit(() -> {
            started.countDown();
            try {
                gate.await();
            } catch (InterruptedException ignored) {
            }
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        pool.submit(() -> {
        });
        pool.submit(() -> {
        });
        AtomicInteger ranRejected = new AtomicInteger();
        assertThrows(RejectedExecutionException.class, () -> pool.submit(ranRejected::incrementAndGet));
        gate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, ranRejected.get());
    }

    @Test
    void rejectsAfterShutdown() throws Exception {
        SimplePool pool = new SimplePool(2, 10);
        pool.shutdown();
        pool.shutdown();
        assertThrows(RejectedExecutionException.class, () -> pool.submit(() -> {
        }));
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void awaitTerminationTimesOutWhileATaskRuns() throws Exception {
        SimplePool pool = new SimplePool(1, 10);
        pool.submit(() -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException ignored) {
            }
        });
        pool.shutdown();
        long t0 = System.nanoTime();
        assertFalse(pool.awaitTermination(100, TimeUnit.MILLISECONDS));
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 500);
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
    }

    @Test
    void noWorkerThreadSurvivesTermination() throws Exception {
        SimplePool pool = new SimplePool(3, 10);
        pool.submit(() -> {
        });
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            while (simpleThreadAlive()) Thread.sleep(10);
        });
    }
}
