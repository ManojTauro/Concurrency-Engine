package dev.conc.p11;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class C01LatencyHistogramTest {

    private static void assertNear(long expected, long actual, String what) {
        assertTrue(Math.abs(actual - expected) <= Math.max(1, expected * 0.01), what + ": expected ~" + expected + " but was " + actual);
    }

    @Test
    void emptyHistogramAnswersZero() {
        LatencyHistogram h = new LatencyHistogram();
        assertEquals(0, h.count());
        assertEquals(0, h.min());
        assertEquals(0, h.max());
        assertEquals(0, h.percentile(50));
    }

    @Test
    void countMinAndMaxAreExact() {
        LatencyHistogram h = new LatencyHistogram();
        for (long v : new long[]{5_000, 123_456_789, 42, 7_777_777}) h.record(v);
        assertEquals(4, h.count());
        assertEquals(42, h.min());
        assertEquals(123_456_789, h.max());
        assertEquals(123_456_789, h.percentile(100), "p100 is exactly the max");
    }

    @Test
    void smallValuesAreExact() {
        LatencyHistogram h = new LatencyHistogram();
        for (long v = 1; v <= 200; v++) h.record(v);
        assertEquals(100, h.percentile(50));
        assertEquals(200, h.percentile(100));
        assertEquals(1, h.percentile(0.5));
    }

    @Test
    void percentilesOfAUniformRangeAreWithinOnePercent() {
        LatencyHistogram h = new LatencyHistogram();
        for (long i = 1; i <= 100_000; i++) h.record(i * 1_000);
        assertNear(50_000_000, h.percentile(50), "p50");
        assertNear(90_000_000, h.percentile(90), "p90");
        assertNear(99_000_000, h.percentile(99), "p99");
        assertNear(99_900_000, h.percentile(99.9), "p99.9");
        assertEquals(100_000_000, h.percentile(100));
    }

    @Test
    void aSlowTailShowsUpInTheHighPercentilesOnly() {
        LatencyHistogram h = new LatencyHistogram();
        for (int i = 0; i < 9_900; i++) h.record(1_000_000);
        for (int i = 0; i < 100; i++) h.record(1_000_000_000);
        assertNear(1_000_000, h.percentile(50), "p50");
        assertNear(1_000_000, h.percentile(98), "p98");
        assertNear(1_000_000_000, h.percentile(99.5), "p99.5");
    }

    @Test
    void randomValuesMatchAnExactSortWithinOnePercent() {
        Random rnd = new Random(11);
        LatencyHistogram h = new LatencyHistogram();
        List<Long> all = new ArrayList<>();
        for (int i = 0; i < 200_000; i++) {
            long v = (long) Math.exp(rnd.nextDouble() * 20);
            all.add(v);
            h.record(v);
        }
        all.sort(null);
        for (double p : new double[]{10, 50, 90, 99, 99.9}) {
            long exact = all.get((int) Math.ceil(p / 100 * all.size()) - 1);
            assertNear(exact, h.percentile(p), "p" + p);
        }
    }

    @Test
    void outOfRangeValuesAreClamped() {
        LatencyHistogram h = new LatencyHistogram();
        h.record(-5);
        h.record(Long.MAX_VALUE);
        assertEquals(2, h.count());
        assertEquals(0, h.min());
        assertEquals(1L << 40, h.max());
    }

    @Test
    void manyThreadsLoseNoSample() throws Exception {
        LatencyHistogram h = new LatencyHistogram();
        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            int seed = t;
            ts.add(new Thread(() -> {
                Random rnd = new Random(seed);
                for (int i = 0; i < 100_000; i++) h.record(1 + rnd.nextInt(5_000_000));
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(800_000, h.count());
        assertTrue(h.percentile(50) > 2_000_000 && h.percentile(50) < 3_000_000);
    }

    @Test
    void mergeAddsTheSamples() {
        LatencyHistogram a = new LatencyHistogram(), b = new LatencyHistogram();
        for (long i = 1; i <= 1_000; i++) a.record(i * 1_000);
        for (long i = 1_001; i <= 2_000; i++) b.record(i * 1_000);
        a.merge(b);
        assertEquals(2_000, a.count());
        assertEquals(2_000_000, a.max());
        assertEquals(1_000, a.min());
        assertNear(1_000_000, a.percentile(50), "merged p50");
        assertEquals(1_000, b.count());
    }

    @Test
    void memoryDoesNotGrowWithTheNumberOfSamples() {
        LatencyHistogram h = new LatencyHistogram();
        Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 1_000; i++) h.record(i * 7919L);
        System.gc();
        long before = rt.totalMemory() - rt.freeMemory();
        for (int i = 0; i < 20_000_000; i++) h.record((i * 7919L) % 1_000_000_000L);
        System.gc();
        long after = rt.totalMemory() - rt.freeMemory();
        assertTrue(after - before < 8_000_000, "heap grew by " + (after - before) + " bytes for 20 million samples");
        assertEquals(20_001_000L, h.count());
    }
}
