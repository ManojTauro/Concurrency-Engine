package dev.conc.p4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class C03PoolSizingTest {

    @Test
    void cpuBoundIsCoresPlusOne() {
        assertEquals(9, Sizing.cpuBound(8));
        assertEquals(2, Sizing.cpuBound(1));
    }

    @Test
    void blockingUsesTheWaitToComputeRatio() {
        assertEquals(16, Sizing.blocking(8, 1.0, 1_000_000, 1_000_000));
        assertEquals(88, Sizing.blocking(8, 1.0, 10_000_000, 1_000_000));
        assertEquals(44, Sizing.blocking(8, 0.5, 10_000_000, 1_000_000));
    }

    @Test
    void blockingRoundsUpAndNeverReturnsZero() {
        assertEquals(2, Sizing.blocking(1, 0.5, 2, 1));
        assertEquals(3, Sizing.blocking(2, 0.5, 1.2, 1));
        assertEquals(1, Sizing.blocking(1, 0.1, 0, 1_000));
    }

    @Test
    void littlesLawGivesRequestsInFlight() {
        assertEquals(50, Sizing.littlesLaw(1_000, 0.05));
        assertEquals(3, Sizing.littlesLaw(10, 0.25));
        assertEquals(1, Sizing.littlesLaw(1, 0.01));
        assertEquals(0, Sizing.littlesLaw(0, 0.5));
    }

    @Test
    void sweepReportsOnePointPerSizeInOrder() {
        List<Sweep.Point> points = Sweep.run(Workloads.sleep(2), new int[]{1, 2, 4}, 60);
        assertEquals(List.of(1, 2, 4), points.stream().map(Sweep.Point::workers).toList());
        points.forEach(p -> assertTrue(p.tasksPerSecond() > 0));
    }

    @Test
    void waitingWorkScalesWithMoreThreads() {
        List<Sweep.Point> points = Sweep.run(Workloads.sleep(10), new int[]{1, 8}, 160);
        double one = points.get(0).tasksPerSecond(), eight = points.get(1).tasksPerSecond();
        assertTrue(eight > 4 * one, "1 worker: " + one + "/s, 8 workers: " + eight + "/s");
    }

    @Test
    void sweepLeavesNoThreadsBehindAndValidatesInput() {
        Sweep.run(Workloads.sleep(1), new int[]{2, 3}, 10);
        assertFalse(Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p4-sweep-")));
        assertThrows(IllegalArgumentException.class, () -> Sweep.run(Workloads.sleep(1), new int[]{1}, 0));
        assertTrue(Sweep.run(Workloads.sleep(1), new int[]{}, 10).isEmpty());
    }

    @Test
    void sweepNamesItsThreads() {
        java.util.Set<String> names = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        Sweep.run(() -> names.add(Thread.currentThread().getName()), new int[]{1}, 5);
        assertEquals(java.util.Set.of("p4-sweep-1"), names);
    }
}
