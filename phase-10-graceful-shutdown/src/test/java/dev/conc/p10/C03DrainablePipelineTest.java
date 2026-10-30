package dev.conc.p10;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class C03DrainablePipelineTest {

    private static Event ev(long id) {
        return new Event(id, "u", "p", EventType.VIEW, 0, id);
    }

    private static boolean workerAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p10-worker-"));
    }

    @Test
    void closeProcessesEverythingAcceptedAndEndsTheWorkers() throws Exception {
        AtomicLong sunk = new AtomicLong();
        DrainablePipeline p = new DrainablePipeline(4, 16, e -> sunk.incrementAndGet());
        for (int i = 0; i < 5_000; i++) assertTrue(p.submit(ev(i)));
        DrainReport r = p.close(Duration.ofSeconds(10));
        assertEquals(new DrainReport(5_000, 5_000, true), r);
        assertEquals(5_000, sunk.get());
        assertFalse(workerAlive());
    }

    @Test
    void submitAfterCloseIsRefused() throws Exception {
        DrainablePipeline p = new DrainablePipeline(2, 4, e -> {
        });
        p.close(Duration.ofSeconds(5));
        assertFalse(p.submit(ev(1)));
    }

    @Test
    void closeIsIdempotentAndSafeFromSeveralThreads() throws Exception {
        DrainablePipeline p = new DrainablePipeline(2, 8, e -> {
        });
        for (int i = 0; i < 100; i++) p.submit(ev(i));
        List<DrainReport> reports = java.util.Collections.synchronizedList(new ArrayList<>());
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ts.add(new Thread(() -> {
                try {
                    reports.add(p.close(Duration.ofSeconds(5)));
                } catch (InterruptedException ignored) {
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(4, reports.size());
        reports.forEach(r -> assertEquals(new DrainReport(100, 100, true), r));
        assertEquals(new DrainReport(100, 100, true), p.close(Duration.ofSeconds(1)));
    }

    @Test
    void everySubmitThatSaidTrueIsProcessedEvenWhenCloseRacesWithThem() throws Exception {
        for (int round = 0; round < 10; round++) {
            AtomicLong sunk = new AtomicLong();
            DrainablePipeline p = new DrainablePipeline(3, 8, e -> sunk.incrementAndGet());
            AtomicLong trues = new AtomicLong();
            List<Thread> submitters = new ArrayList<>();
            for (int s = 0; s < 4; s++) {
                int base = s * 1_000_000;
                submitters.add(new Thread(() -> {
                    try {
                        for (int i = 0; ; i++) {
                            if (!p.submit(ev(base + i))) return;
                            trues.incrementAndGet();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }));
            }
            submitters.forEach(Thread::start);
            Thread.sleep(50);
            DrainReport r = p.close(Duration.ofSeconds(10));
            for (Thread t : submitters) t.join(5_000);
            assertTrue(r.drained(), "round " + round);
            assertEquals(trues.get(), r.accepted(), "round " + round + ": accepted != submits that returned true");
            assertEquals(r.accepted(), r.processed(), "round " + round);
            assertEquals(r.accepted(), sunk.get(), "round " + round);
            assertTrue(r.accepted() > 0);
        }
    }

    @Test
    void aSinkThatThrowsDoesNotStopTheWorkersOrLoseCount() throws Exception {
        DrainablePipeline p = new DrainablePipeline(2, 8, e -> {
            if (e.eventId() % 2 == 0) throw new IllegalStateException("bad");
        });
        for (int i = 0; i < 1_000; i++) p.submit(ev(i));
        assertEquals(new DrainReport(1_000, 1_000, true), p.close(Duration.ofSeconds(10)));
    }

    @Test
    void whenTheDrainTakesTooLongCloseGivesUpAndInterruptsTheWorkers() throws Exception {
        CountDownLatch never = new CountDownLatch(1);
        DrainablePipeline p = new DrainablePipeline(1, 10, e -> {
            try {
                never.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });
        for (int i = 0; i < 5; i++) p.submit(ev(i));
        Thread.sleep(100);
        long t0 = System.nanoTime();
        DrainReport r = p.close(Duration.ofMillis(200));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertFalse(r.drained());
        assertEquals(5, r.accepted());
        assertTrue(r.processed() < 5);
        assertTrue(ms >= 180 && ms < 2500, "took " + ms + " ms");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (workerAlive() && System.nanoTime() < deadline) Thread.sleep(20);
        assertFalse(workerAlive(), "the interrupted worker should have ended");
    }

    @Test
    void theShutdownHookIsAnUnstartedThreadThatDrainsThePipeline() throws Exception {
        AtomicLong sunk = new AtomicLong();
        DrainablePipeline p = new DrainablePipeline(2, 8, e -> sunk.incrementAndGet());
        for (int i = 0; i < 200; i++) p.submit(ev(i));
        Thread hook = p.shutdownHook(Duration.ofSeconds(5));
        assertEquals(Thread.State.NEW, hook.getState());
        hook.start();
        hook.join(10_000);
        assertEquals(200, sunk.get());
        assertFalse(p.submit(ev(999)));
    }

    @Test
    void invalidSizesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DrainablePipeline(0, 4, e -> {
        }));
        assertThrows(IllegalArgumentException.class, () -> new DrainablePipeline(1, 0, e -> {
        }));
    }
}
