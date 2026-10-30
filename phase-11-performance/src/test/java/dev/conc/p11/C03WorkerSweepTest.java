package dev.conc.p11;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class C03WorkerSweepTest {

    @Test
    void onePointPerWorkerCountInOrder() {
        List<SweepPoint> pts = WorkerSweep.run(Work.sleepMillis(2), new int[]{1, 2, 4}, 60);
        assertEquals(List.of(1, 2, 4), pts.stream().map(SweepPoint::workers).toList());
        for (SweepPoint p : pts) {
            assertTrue(p.throughputPerSecond() > 0);
            assertTrue(p.p99Nanos() >= p.p50Nanos());
            assertTrue(p.p50Nanos() > 0);
        }
    }

    @Test
    void waitingWorkScalesAndTheQueueingDelayShrinks() {
        List<SweepPoint> pts = WorkerSweep.run(Work.sleepMillis(5), new int[]{1, 8}, 200);
        SweepPoint one = pts.get(0), eight = pts.get(1);
        assertTrue(eight.throughputPerSecond() > 4 * one.throughputPerSecond(), one.throughputPerSecond() + " vs " + eight.throughputPerSecond());
        assertTrue(one.p99Nanos() > 3 * eight.p99Nanos(), "p99 with 1 worker " + one.p99Nanos() / 1e6 + " ms, with 8: " + eight.p99Nanos() / 1e6 + " ms");
    }

    @Test
    void throughputIsEventsOverTheWholeRun() {
        List<SweepPoint> pts = WorkerSweep.run(Work.sleepMillis(10), new int[]{1}, 50);
        double tp = pts.get(0).throughputPerSecond();
        assertTrue(tp > 60 && tp < 100, "50 tasks of 10 ms on one thread cannot be faster than 100/s: " + tp);
    }

    @Test
    void noThreadIsLeftBehindAndBadInputIsRejected() {
        WorkerSweep.run(Work.sleepMillis(1), new int[]{2, 3}, 10);
        assertFalse(Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p11-")));
        assertThrows(IllegalArgumentException.class, () -> WorkerSweep.run(Work.sleepMillis(1), new int[]{1}, 0));
        assertTrue(WorkerSweep.run(Work.sleepMillis(1), new int[]{}, 10).isEmpty());
    }

    @Test
    void csvHasAHeaderAndOneLinePerPoint() {
        String csv = WorkerSweep.toCsv(List.of(
                new SweepPoint(1, 198.04, 2_500_000, 31_250_000),
                new SweepPoint(8, 1523.449, 1_000_000, 4_000_500)));
        assertEquals("workers,throughput_per_s,p50_ms,p99_ms\n1,198.0,2.500,31.250\n8,1523.4,1.000,4.001", csv);
        assertEquals("workers,throughput_per_s,p50_ms,p99_ms", WorkerSweep.toCsv(List.of()));
    }
}
