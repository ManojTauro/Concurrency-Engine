package dev.conc.p3;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C02RoundRunnerTest {

    private static boolean p3ThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p3-"));
    }

    @Test
    void noThreadStartsARoundBeforeEveryoneFinishedThePreviousOne() {
        int threads = 6, rounds = 50;
        AtomicInteger[] finished = new AtomicInteger[rounds];
        for (int r = 0; r < rounds; r++) finished[r] = new AtomicInteger();
        AtomicInteger violations = new AtomicInteger();
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> RoundRunner.run(threads, rounds, (t, r) -> {
            if (r > 0 && finished[r - 1].get() != threads) violations.incrementAndGet();
            if (t == 0) Thread.sleep(1);
            finished[r].incrementAndGet();
        }));
        assertEquals(0, violations.get());
        for (int r = 0; r < rounds; r++) assertEquals(threads, finished[r].get());
    }

    @Test
    void everyThreadRunsEveryRoundOnce() {
        AtomicInteger[][] runs = new AtomicInteger[4][10];
        for (int t = 0; t < 4; t++) for (int r = 0; r < 10; r++) runs[t][r] = new AtomicInteger();
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> RoundRunner.run(4, 10, (t, r) -> runs[t][r].incrementAndGet()));
        for (int t = 0; t < 4; t++) for (int r = 0; r < 10; r++) assertEquals(1, runs[t][r].get());
    }

    @Test
    void aFailingTaskFailsTheRunWithoutHangingTheOthers() {
        RoundFailedException ex = assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(RoundFailedException.class, () -> RoundRunner.run(6, 20, (t, r) -> {
                    if (t == 2 && r == 3) throw new IllegalStateException("boom");
                })));
        assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
        assertFalse(p3ThreadAlive());
    }

    @Test
    void aFailureInTheVeryFirstRoundIsReported() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(RoundFailedException.class, () -> RoundRunner.run(3, 5, (t, r) -> {
                    if (t == 0) throw new RuntimeException("early");
                })));
    }

    @Test
    void zeroRoundsReturnsAndBadArgumentsAreRejected() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> RoundRunner.run(3, 0, (t, r) -> fail()));
        assertThrows(IllegalArgumentException.class, () -> RoundRunner.run(0, 1, (t, r) -> {
        }));
        assertThrows(IllegalArgumentException.class, () -> RoundRunner.run(1, -1, (t, r) -> {
        }));
    }

    @Test
    void threadsAreNamedAndGoneAfterwards() {
        java.util.Set<String> names = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> RoundRunner.run(3, 4, (t, r) -> names.add(Thread.currentThread().getName())));
        assertEquals(java.util.Set.of("p3-round-0", "p3-round-1", "p3-round-2"), names);
        assertFalse(p3ThreadAlive());
    }
}
