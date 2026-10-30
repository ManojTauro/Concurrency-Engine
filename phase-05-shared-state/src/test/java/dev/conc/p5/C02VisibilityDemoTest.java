package dev.conc.p5;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class C02VisibilityDemoTest {

    private static long nonDaemonThreads() {
        return Thread.getAllStackTraces().keySet().stream().filter(t -> t.isAlive() && !t.isDaemon()).count();
    }

    @RepeatedTest(3)
    void volatileFlagAlwaysStops() {
        assertTrue(VisibilityDemo.volatileFlagStops(Duration.ofMillis(200), Duration.ofSeconds(2)));
    }

    @Test
    void plainFlagReturnsWithinTheGiveUpTime() {
        long t0 = System.nanoTime();
        VisibilityDemo.plainFlagStops(Duration.ofMillis(200), Duration.ofSeconds(1));
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(elapsedMs < 2_500, "took " + elapsedMs + " ms; the call must give up after giveUp");
    }

    @Test
    void aWorkerThatNeverStopsDoesNotKeepTheJvmAlive() {
        long before = nonDaemonThreads();
        VisibilityDemo.plainFlagStops(Duration.ofMillis(100), Duration.ofMillis(500));
        assertEquals(before, nonDaemonThreads(), "the worker must be a daemon thread");
    }

    @Test
    void waitsForRunForBeforeStopping() {
        long t0 = System.nanoTime();
        VisibilityDemo.volatileFlagStops(Duration.ofMillis(300), Duration.ofSeconds(2));
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(elapsedMs >= 280, "returned after only " + elapsedMs + " ms");
    }
}
