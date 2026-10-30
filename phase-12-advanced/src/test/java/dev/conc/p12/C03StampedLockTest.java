package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class C03StampedLockTest {

    @Test
    void startsAtZeroAndReadsWhatWasSet() {
        ScoreBoard b = new ScoreBoard();
        assertEquals(new Snapshot(0, 0), b.read());
        b.set(21);
        assertEquals(new Snapshot(21, 42), b.read());
    }

    @Test
    void readsWithoutAWriterAreAllOptimistic() {
        ScoreBoard b = new ScoreBoard();
        b.set(5);
        for (int i = 0; i < 100_000; i++) b.read();
        assertEquals(100_000, b.optimisticReads());
        assertEquals(0, b.fallbackReads());
    }

    @Test
    void aReaderNeverSeesATornSnapshotWhileAWriterRuns() throws Exception {
        ScoreBoard b = new ScoreBoard();
        AtomicBoolean stop = new AtomicBoolean();
        AtomicReference<String> failure = new AtomicReference<>();
        AtomicLong reads = new AtomicLong();
        List<Thread> readers = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            readers.add(new Thread(() -> {
                long n = 0;
                while (!stop.get()) {
                    Snapshot s = b.read();
                    n++;
                    if (s.b() != 2 * s.a()) failure.compareAndSet(null, "torn: a=" + s.a() + " b=" + s.b());
                }
                reads.addAndGet(n);
            }));
        }
        Thread writer = new Thread(() -> {
            for (long x = 1; x <= 300_000 && failure.get() == null; x++) b.set(x);
        });
        readers.forEach(Thread::start);
        writer.start();
        writer.join();
        stop.set(true);
        for (Thread t : readers) t.join();
        assertNull(failure.get());
        assertEquals(reads.get(), b.optimisticReads() + b.fallbackReads(), "every read is counted exactly once");
        assertTrue(b.optimisticReads() > 0);
    }

    @Test
    void theFinalValueIsTheLastWrite() throws Exception {
        ScoreBoard b = new ScoreBoard();
        List<Thread> writers = new ArrayList<>();
        for (int w = 0; w < 4; w++) {
            writers.add(new Thread(() -> {
                for (int i = 0; i < 50_000; i++) b.set(7);
            }));
        }
        writers.forEach(Thread::start);
        for (Thread t : writers) t.join();
        assertEquals(new Snapshot(7, 14), b.read());
    }
}
