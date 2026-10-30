package dev.conc.p11;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C02LoadGeneratorsTest {

    /** Takes 1 ms, except the 100th call which stalls for 400 ms (a GC pause, a lock, a slow disk). */
    private static Service stallingService() {
        AtomicInteger calls = new AtomicInteger();
        return () -> Thread.sleep(calls.incrementAndGet() == 100 ? 400 : 1);
    }

    private static boolean p11ThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p11-"));
    }

    @Test
    void closedLoopHidesTheStall() {
        ListRecorder rec = new ListRecorder();
        long n = ClosedLoopGenerator.run(stallingService(), 1, Duration.ofMillis(1_500), rec);
        assertTrue(n > 400, "requests: " + n);
        assertEquals(n, rec.count());
        assertTrue(rec.maxNanos() >= 390_000_000L, "the stalled request itself must show 400 ms");
        assertTrue(rec.percentileNanos(99) < 100_000_000L, "p99 " + rec.percentileNanos(99) / 1e6 + " ms: one slow request in several hundred");
    }

    @Test
    void openLoopSeesTheQueueThatBuiltUpBehindTheStall() {
        ListRecorder rec = new ListRecorder();
        long n = OpenLoopGenerator.run(stallingService(), 500, Duration.ofMillis(1_500), 1, rec);
        assertTrue(n >= 740 && n <= 760, "requests issued: " + n);
        assertEquals(n, rec.count());
        assertTrue(rec.percentileNanos(99) > 250_000_000L, "p99 " + rec.percentileNanos(99) / 1e6 + " ms: 200 requests were due during the stall");
        assertTrue(rec.percentileNanos(50) < 200_000_000L);
    }

    @Test
    void openLoopWithAFastEnoughServiceStaysFast() {
        ListRecorder rec = new ListRecorder();
        long n = OpenLoopGenerator.run(() -> Thread.sleep(1), 200, Duration.ofMillis(1_000), 4, rec);
        assertTrue(n >= 190 && n <= 210, "issued " + n);
        assertTrue(rec.percentileNanos(99) < 60_000_000L, "p99 " + rec.percentileNanos(99) / 1e6 + " ms");
    }

    @Test
    void openLoopFollowsTheScheduleNoMatterHowSlowTheService() {
        ListRecorder rec = new ListRecorder();
        long t0 = System.nanoTime();
        long n = OpenLoopGenerator.run(() -> Thread.sleep(30), 100, Duration.ofMillis(500), 1, rec);
        assertTrue(n >= 48 && n <= 52, "issued " + n + " even though the service only manages 33/s");
        assertTrue(rec.percentileNanos(99) > 400_000_000L, "the backlog should show up as latency");
        assertTrue((System.nanoTime() - t0) / 1_000_000 > 1_000, "run() waits for every request to complete");
    }

    @Test
    void aFailingCallIsStillRecorded() {
        ListRecorder rec = new ListRecorder();
        long n = ClosedLoopGenerator.run(() -> {
            Thread.sleep(2);
            throw new IllegalStateException("boom");
        }, 2, Duration.ofMillis(200), rec);
        assertEquals(n, rec.count());
        ListRecorder rec2 = new ListRecorder();
        long m = OpenLoopGenerator.run(() -> {
            throw new IllegalStateException("boom");
        }, 100, Duration.ofMillis(200), 2, rec2);
        assertEquals(m, rec2.count());
    }

    @Test
    void noThreadIsLeftBehindAndBadArgumentsAreRejected() {
        ClosedLoopGenerator.run(() -> {
        }, 3, Duration.ofMillis(100), new ListRecorder());
        OpenLoopGenerator.run(() -> {
        }, 100, Duration.ofMillis(100), 3, new ListRecorder());
        assertFalse(p11ThreadAlive());
        assertThrows(IllegalArgumentException.class, () -> OpenLoopGenerator.run(() -> {
        }, 0, Duration.ofMillis(10), 1, new ListRecorder()));
        assertThrows(IllegalArgumentException.class, () -> OpenLoopGenerator.run(() -> {
        }, 10, Duration.ofMillis(10), 0, new ListRecorder()));
    }
}
