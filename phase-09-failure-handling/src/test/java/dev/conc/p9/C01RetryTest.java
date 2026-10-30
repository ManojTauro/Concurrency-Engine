package dev.conc.p9;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C01RetryTest {

    private final List<Duration> slept = new ArrayList<>();
    private final Sleeper sleeper = slept::add;

    private static RetryPolicy policy(int attempts, long baseMs, long maxMs, Jitter j) {
        return new RetryPolicy(attempts, Duration.ofMillis(baseMs), Duration.ofMillis(maxMs), j);
    }

    @Test
    void successOnTheFirstTryNeverSleeps() throws Exception {
        assertEquals("ok", Retry.call(() -> "ok", policy(5, 100, 1000, Jitter.NONE), sleeper, new Random(1)));
        assertTrue(slept.isEmpty());
    }

    @Test
    void waitsDoubleEachTimeAndStopsAtTheCap() {
        AtomicInteger calls = new AtomicInteger();
        RetriesExhaustedException ex = assertThrows(RetriesExhaustedException.class, () ->
                Retry.call(() -> {
                    calls.incrementAndGet();
                    throw new IOException("down");
                }, policy(7, 100, 1000, Jitter.NONE), sleeper, new Random(1)));
        assertEquals(7, calls.get());
        assertEquals(7, ex.attempts());
        assertEquals(List.of(100L, 200L, 400L, 800L, 1000L, 1000L), slept.stream().map(Duration::toMillis).toList());
    }

    @Test
    void succeedsAfterSomeFailures() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        String r = Retry.call(() -> {
            if (calls.incrementAndGet() < 3) throw new IOException("flaky");
            return "done";
        }, policy(5, 10, 100, Jitter.NONE), sleeper, new Random(1));
        assertEquals("done", r);
        assertEquals(3, calls.get());
        assertEquals(2, slept.size());
    }

    @Test
    void theLastExceptionIsTheCause() {
        AtomicInteger calls = new AtomicInteger();
        RetriesExhaustedException ex = assertThrows(RetriesExhaustedException.class, () ->
                Retry.call(() -> {
                    throw new IOException("failure " + calls.incrementAndGet());
                }, policy(3, 1, 10, Jitter.NONE), sleeper, new Random(1)));
        assertEquals("failure 3", ex.getCause().getMessage());
    }

    @Test
    void aPermanentFailureIsNotRetried() {
        AtomicInteger calls = new AtomicInteger();
        PermanentFailureException ex = assertThrows(PermanentFailureException.class, () ->
                Retry.call(() -> {
                    calls.incrementAndGet();
                    throw new PermanentFailureException("bad request");
                }, policy(5, 10, 100, Jitter.NONE), sleeper, new Random(1)));
        assertEquals("bad request", ex.getMessage());
        assertEquals(1, calls.get());
        assertTrue(slept.isEmpty());
    }

    @Test
    void anInterruptionStopsEverythingAtOnce() {
        AtomicInteger calls = new AtomicInteger();
        assertThrows(InterruptedException.class, () -> Retry.call(() -> {
            calls.incrementAndGet();
            throw new InterruptedException();
        }, policy(5, 10, 100, Jitter.NONE), sleeper, new Random(1)));
        assertEquals(1, calls.get());

        AtomicInteger calls2 = new AtomicInteger();
        Sleeper interrupted = d -> {
            throw new InterruptedException();
        };
        assertThrows(InterruptedException.class, () -> Retry.call(() -> {
            calls2.incrementAndGet();
            throw new IOException("x");
        }, policy(5, 10, 100, Jitter.NONE), interrupted, new Random(1)));
        assertEquals(1, calls2.get());
    }

    @Test
    void fullJitterDrawsOneRandomWaitUpToEachCap() {
        assertThrows(RetriesExhaustedException.class, () -> Retry.call(() -> {
            throw new IOException();
        }, policy(6, 100, 1000, Jitter.FULL), sleeper, new Random(7)));
        Random replay = new Random(7);
        long[] caps = {100, 200, 400, 800, 1000};
        assertEquals(5, slept.size());
        for (int i = 0; i < 5; i++) {
            long expected = replay.nextLong(Duration.ofMillis(caps[i]).toNanos() + 1);
            assertEquals(expected, slept.get(i).toNanos(), "wait " + (i + 1));
        }
    }

    @Test
    void theDoublingNeverOverflows() {
        assertThrows(RetriesExhaustedException.class, () -> Retry.call(() -> {
            throw new IOException();
        }, new RetryPolicy(90, Duration.ofSeconds(1), Duration.ofSeconds(5), Jitter.NONE), sleeper, new Random(1)));
        assertEquals(89, slept.size());
        for (int i = 0; i < slept.size(); i++) {
            long expected = Math.min(5_000, 1_000L << Math.min(i, 3));
            assertEquals(expected, slept.get(i).toMillis(), "wait " + (i + 1));
        }
    }

    @Test
    void oneAttemptMeansNoRetryAndNoWait() {
        RetriesExhaustedException ex = assertThrows(RetriesExhaustedException.class, () -> Retry.call(() -> {
            throw new IOException();
        }, policy(1, 100, 1000, Jitter.NONE), sleeper, new Random(1)));
        assertEquals(1, ex.attempts());
        assertTrue(slept.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> Retry.call(() -> "x", policy(0, 1, 1, Jitter.NONE), sleeper, new Random(1)));
    }
}
