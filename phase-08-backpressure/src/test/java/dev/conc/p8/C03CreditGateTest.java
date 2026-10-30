package dev.conc.p8;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C03CreditGateTest {

    private static List<Event> events(int n) {
        List<Event> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) list.add(new Event(i, "u", "p", EventType.VIEW, 0, i));
        return list;
    }

    @Test
    void creditsAreLimitedAndInFlightIsCounted() throws Exception {
        CreditGate g = new CreditGate(2);
        g.acquire();
        g.acquire();
        assertEquals(2, g.inFlight());
        assertFalse(g.tryAcquire(Duration.ofMillis(80)));
        g.release();
        assertEquals(1, g.inFlight());
        assertTrue(g.tryAcquire(Duration.ofMillis(80)));
        assertEquals(2, g.inFlight());
    }

    @Test
    void acquireWaitsForARelease() throws Exception {
        CreditGate g = new CreditGate(1);
        g.acquire();
        AtomicInteger got = new AtomicInteger();
        Thread t = new Thread(() -> {
            try {
                g.acquire();
                got.set(1);
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        Thread.sleep(120);
        assertEquals(0, got.get());
        g.release();
        t.join(2000);
        assertEquals(1, got.get());
    }

    @Test
    void releasingMoreThanWasTakenIsAnError() {
        CreditGate g = new CreditGate(2);
        assertThrows(IllegalStateException.class, g::release);
        assertThrows(IllegalArgumentException.class, () -> new CreditGate(0));
    }

    @Test
    void neverMoreThanTheLimitInFlightUnderContention() throws Exception {
        CreditGate g = new CreditGate(5);
        AtomicInteger current = new AtomicInteger();
        AtomicInteger max = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            ts.add(new Thread(() -> {
                try {
                    for (int k = 0; k < 2_000; k++) {
                        g.acquire();
                        int now = current.incrementAndGet();
                        max.accumulateAndGet(now, Math::max);
                        current.decrementAndGet();
                        g.release();
                    }
                } catch (InterruptedException ignored) {
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertTrue(max.get() <= 5, "max in flight " + max.get());
        assertEquals(0, g.inFlight());
    }

    @Test
    void pipelineKeepsQueuedPlusRunningWithinTheCredits() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            AtomicInteger sampledMax = new AtomicInteger();
            AtomicInteger stop = new AtomicInteger();
            Thread sampler = new Thread(() -> {
                while (stop.get() == 0) {
                    sampledMax.accumulateAndGet(pool.getQueue().size() + pool.getActiveCount(), Math::max);
                    Thread.onSpinWait();
                }
            });
            sampler.start();
            long handled = CreditPipeline.run(events(400), 10, pool, e -> {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ignored) {
                }
            });
            stop.set(1);
            sampler.join();
            assertEquals(400, handled);
            // a worker gives its credit back a moment before it stops counting as active, so allow the pool size on top
            assertTrue(sampledMax.get() <= 10 + 4, "queued + running reached " + sampledMax.get() + " with 10 credits");
            assertTrue(sampledMax.get() >= 4, "the pool should have been busy: " + sampledMax.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aThrowingSinkNeverLeaksACreditOrStallsTheRun() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 3, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            AtomicInteger attempts = new AtomicInteger();
            long handled = assertTimeoutPreemptively(Duration.ofSeconds(20), () -> CreditPipeline.run(events(300), 4, pool, e -> {
                attempts.incrementAndGet();
                if (e.eventId() % 10 == 0) throw new IllegalStateException("bad event");
            }));
            assertEquals(300, attempts.get());
            assertEquals(270, handled);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void oneCreditMeansOneAtATime() throws Exception {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        try {
            AtomicInteger current = new AtomicInteger();
            AtomicInteger max = new AtomicInteger();
            CreditPipeline.run(events(100), 1, pool, e -> {
                max.accumulateAndGet(current.incrementAndGet(), Math::max);
                Thread.onSpinWait();
                current.decrementAndGet();
            });
            assertEquals(1, max.get());
        } finally {
            pool.shutdownNow();
        }
    }
}
