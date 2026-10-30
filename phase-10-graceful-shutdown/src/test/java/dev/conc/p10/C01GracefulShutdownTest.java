package dev.conc.p10;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class C01GracefulShutdownTest {

    @Test
    void anIdlePoolClosesCleanly() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ShutdownReport r = GracefulShutdown.close(pool, Duration.ofSeconds(2));
        assertTrue(r.clean());
        assertTrue(r.terminated());
        assertTrue(r.neverStarted().isEmpty());
        assertTrue(pool.isTerminated());
    }

    @Test
    void queuedWorkThatFitsInTheGracePeriodIsFinished() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        AtomicInteger done = new AtomicInteger();
        for (int i = 0; i < 5; i++) pool.execute(() -> {
            try {
                Thread.sleep(40);
            } catch (InterruptedException ignored) {
            }
            done.incrementAndGet();
        });
        ShutdownReport r = GracefulShutdown.close(pool, Duration.ofSeconds(3));
        assertTrue(r.clean());
        assertEquals(5, done.get());
    }

    @Test
    void whenTheGraceRunsOutQueuedTasksAreReturnedAndRunningOnesInterrupted() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        AtomicBoolean interrupted = new AtomicBoolean();
        AtomicBoolean started = new AtomicBoolean();
        pool.execute(() -> {
            started.set(true);
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        Runnable q1 = () -> {
        }, q2 = () -> {
        }, q3 = () -> {
        };
        pool.execute(q1);
        pool.execute(q2);
        pool.execute(q3);
        while (!started.get()) Thread.onSpinWait();
        long t0 = System.nanoTime();
        ShutdownReport r = GracefulShutdown.close(pool, Duration.ofMillis(200));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertFalse(r.clean());
        assertTrue(r.terminated());
        assertEquals(java.util.List.of(q1, q2, q3), r.neverStarted());
        assertTrue(interrupted.get());
        assertTrue(ms >= 180 && ms < 1500, "took " + ms + " ms");
    }

    @Test
    void aTaskThatIgnoresInterruptsCannotHoldCloseForLong() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        AtomicBoolean stop = new AtomicBoolean();
        AtomicBoolean started = new AtomicBoolean();
        pool.execute(() -> {
            started.set(true);
            while (!stop.get()) Thread.onSpinWait();
        });
        while (!started.get()) Thread.onSpinWait();
        long t0 = System.nanoTime();
        ShutdownReport r = GracefulShutdown.close(pool, Duration.ofMillis(100));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        stop.set(true);
        assertFalse(r.clean());
        assertFalse(r.terminated());
        assertTrue(ms >= 900 && ms < 2500, "waited " + ms + " ms: 100 ms grace + one fixed second");
    }

    @Test
    void anInterruptedCallerForcesShutdownAndKeepsItsInterruptFlag() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        AtomicBoolean started = new AtomicBoolean();
        pool.execute(() -> {
            started.set(true);
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException ignored) {
            }
        });
        while (!started.get()) Thread.onSpinWait();
        AtomicReference<ShutdownReport> report = new AtomicReference<>();
        AtomicBoolean flagAfter = new AtomicBoolean();
        Thread caller = new Thread(() -> {
            report.set(GracefulShutdown.close(pool, Duration.ofSeconds(30)));
            flagAfter.set(Thread.currentThread().isInterrupted());
        });
        caller.start();
        Thread.sleep(150);
        caller.interrupt();
        caller.join(5_000);
        assertFalse(caller.isAlive(), "close() should return soon after the caller is interrupted");
        assertNotNull(report.get());
        assertFalse(report.get().clean());
        assertTrue(flagAfter.get(), "the interrupt flag must be set again");
        assertTrue(pool.isShutdown());
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void closingAnAlreadyShutDownPoolIsFine() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.shutdown();
        ShutdownReport r = GracefulShutdown.close(pool, Duration.ofSeconds(1));
        assertTrue(r.clean());
    }
}
