package dev.conc.p9;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class C03CircuitBreakerTest {

    private final AtomicLong now = new AtomicLong();
    private final Clock clock = now::get;

    private static final long SEC = 1_000_000_000L;

    private void fail(CircuitBreaker b) {
        assertThrows(IOException.class, () -> b.call(() -> {
            throw new IOException("down");
        }));
    }

    @Test
    void opensAfterTheThresholdOfConsecutiveFailures() throws Exception {
        CircuitBreaker b = new CircuitBreaker(3, Duration.ofSeconds(10), clock);
        fail(b);
        fail(b);
        assertEquals(State.CLOSED, b.state());
        fail(b);
        assertEquals(State.OPEN, b.state());
    }

    @Test
    void aSuccessResetsTheFailureCount() throws Exception {
        CircuitBreaker b = new CircuitBreaker(3, Duration.ofSeconds(10), clock);
        fail(b);
        fail(b);
        assertEquals("ok", b.call(() -> "ok"));
        fail(b);
        fail(b);
        assertEquals(State.CLOSED, b.state(), "2 + success + 2 are not 3 in a row");
        fail(b);
        assertEquals(State.OPEN, b.state());
    }

    @Test
    void anOpenBreakerRefusesWithoutCallingTheAction() {
        CircuitBreaker b = new CircuitBreaker(1, Duration.ofSeconds(10), clock);
        fail(b);
        AtomicInteger calls = new AtomicInteger();
        for (int i = 0; i < 5; i++) {
            assertThrows(CircuitOpenException.class, () -> b.call(() -> {
                calls.incrementAndGet();
                return 1;
            }));
        }
        assertEquals(0, calls.get());
        now.addAndGet(9 * SEC);
        assertThrows(CircuitOpenException.class, () -> b.call(() -> 1));
        assertEquals(State.OPEN, b.state());
    }

    @Test
    void afterThePauseOneTrialCallDecidesTheNextState() throws Exception {
        CircuitBreaker b = new CircuitBreaker(2, Duration.ofSeconds(10), clock);
        fail(b);
        fail(b);
        now.addAndGet(10 * SEC);
        assertEquals(State.HALF_OPEN, b.state());
        assertEquals("trial", b.call(() -> "trial"));
        assertEquals(State.CLOSED, b.state());
        fail(b);
        assertEquals(State.CLOSED, b.state(), "the failure count started again from 0");
    }

    @Test
    void aFailedTrialReopensAndRestartsTheClock() {
        CircuitBreaker b = new CircuitBreaker(1, Duration.ofSeconds(10), clock);
        fail(b);
        now.addAndGet(10 * SEC);
        fail(b);
        assertEquals(State.OPEN, b.state());
        now.addAndGet(9 * SEC);
        assertEquals(State.OPEN, b.state(), "the 10 s start again from the failed trial");
        assertThrows(CircuitOpenException.class, () -> b.call(() -> 1));
        now.addAndGet(1 * SEC);
        assertEquals(State.HALF_OPEN, b.state());
    }

    @Test
    void onlyOneCallIsLetThroughWhileTheTrialIsRunning() throws Exception {
        CircuitBreaker b = new CircuitBreaker(1, Duration.ofSeconds(1), clock);
        fail(b);
        now.addAndGet(SEC);
        CountDownLatch inTrial = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        AtomicInteger actionCalls = new AtomicInteger();
        AtomicInteger refused = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ts.add(new Thread(() -> {
                try {
                    b.call(() -> {
                        actionCalls.incrementAndGet();
                        inTrial.countDown();
                        finish.await();
                        return 1;
                    });
                } catch (CircuitOpenException e) {
                    refused.incrementAndGet();
                } catch (Exception ignored) {
                }
            }));
        }
        ts.forEach(Thread::start);
        assertTrue(inTrial.await(5, TimeUnit.SECONDS));
        Thread.sleep(200);
        assertEquals(1, actionCalls.get());
        assertEquals(7, refused.get());
        assertEquals(State.HALF_OPEN, b.state());
        finish.countDown();
        for (Thread t : ts) t.join();
        assertEquals(State.CLOSED, b.state());
    }

    @Test
    void failuresFromManyThreadsAreAllCounted() throws Exception {
        int threshold = 100;
        CircuitBreaker b = new CircuitBreaker(threshold, Duration.ofSeconds(10), clock);
        AtomicInteger actionCalls = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ts.add(new Thread(() -> {
                for (int k = 0; k < 50; k++) {
                    try {
                        b.call(() -> {
                            actionCalls.incrementAndGet();
                            throw new IOException();
                        });
                    } catch (Exception ignored) {
                    }
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(State.OPEN, b.state());
        assertTrue(actionCalls.get() >= threshold && actionCalls.get() < threshold + 8,
                "the action ran " + actionCalls.get() + " times for a threshold of " + threshold);
    }

    @Test
    void theActionRunsOutsideAnyLock() throws Exception {
        CircuitBreaker b = new CircuitBreaker(5, Duration.ofSeconds(10), clock);
        CountDownLatch inAction = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread slow = new Thread(() -> {
            try {
                b.call(() -> {
                    inAction.countDown();
                    release.await();
                    return 1;
                });
            } catch (Exception ignored) {
            }
        });
        slow.start();
        assertTrue(inAction.await(5, TimeUnit.SECONDS));
        long t0 = System.nanoTime();
        assertEquals("fast", b.call(() -> "fast"));
        assertEquals(State.CLOSED, b.state());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 1000, "a slow call must not block other callers");
        release.countDown();
        slow.join();
    }

    @Test
    void thresholdBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(0, Duration.ofSeconds(1), clock));
    }
}
