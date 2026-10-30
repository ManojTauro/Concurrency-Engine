package dev.conc.p2;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/** What every correct bounded buffer must do. */
abstract class BufferContract {

    abstract Buffer<Integer> create(int capacity);

    @Test
    void rejectsCapacityBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> create(0));
    }

    @Test
    void rejectsNullItems() {
        Buffer<Integer> b = create(2);
        assertThrows(NullPointerException.class, () -> b.put(null));
    }

    @Test
    void fifoOrderAndSize() throws Exception {
        Buffer<Integer> b = create(5);
        for (int i = 1; i <= 5; i++) b.put(i);
        assertEquals(5, b.size());
        for (int i = 1; i <= 5; i++) assertEquals(i, b.take());
        assertEquals(0, b.size());
    }

    @Test
    void wrapsAroundManyTimes() throws Exception {
        Buffer<Integer> b = create(3);
        for (int i = 0; i < 100; i++) {
            b.put(i);
            assertEquals(i, b.take());
        }
    }

    @Test
    void putBlocksWhenFull() throws Exception {
        Buffer<Integer> b = create(2);
        b.put(1);
        b.put(2);
        Thread t = new Thread(() -> {
            try {
                b.put(3);
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        Thread.sleep(150);
        assertTrue(t.isAlive(), "put should wait while full");
        assertEquals(2, b.size());
        assertEquals(1, b.take());
        t.join(2000);
        assertFalse(t.isAlive(), "put should finish once space is free");
        assertEquals(2, b.size());
    }

    @Test
    void takeBlocksWhenEmpty() throws Exception {
        Buffer<Integer> b = create(2);
        AtomicReference<Integer> got = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try {
                got.set(b.take());
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        Thread.sleep(150);
        assertTrue(t.isAlive(), "take should wait while empty");
        b.put(7);
        t.join(2000);
        assertFalse(t.isAlive());
        assertEquals(7, got.get());
    }

    @Test
    void blockedTakeIsInterruptible() throws Exception {
        Buffer<Integer> b = create(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            try {
                b.take();
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();
        t.join(2000);
        assertTrue(interrupted.get());
    }

    @Test
    void blockedPutIsInterruptible() throws Exception {
        Buffer<Integer> b = create(1);
        b.put(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread t = new Thread(() -> {
            try {
                b.put(2);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        t.start();
        Thread.sleep(100);
        t.interrupt();
        t.join(2000);
        assertTrue(interrupted.get());
        assertEquals(1, b.size());
    }

    @Test
    void oneProducerOneConsumerKeepOrder() {
        Buffer<Integer> b = create(8);
        int n = 100_000;
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            Thread p = new Thread(() -> {
                try {
                    for (int i = 0; i < n; i++) b.put(i);
                } catch (InterruptedException ignored) {
                }
            });
            p.start();
            for (int i = 0; i < n; i++) assertEquals(i, b.take());
            p.join();
        });
    }

    @Test
    void manyProducersManyConsumersDeliverEachItemOnce() {
        Buffer<Integer> b = create(16);
        int producers = 4, consumers = 4, each = 25_000, total = producers * each;
        AtomicIntegerArray seen = new AtomicIntegerArray(total);
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            List<Thread> ps = new ArrayList<>(), cs = new ArrayList<>();
            for (int p = 0; p < producers; p++) {
                int base = p * each;
                ps.add(new Thread(() -> {
                    try {
                        for (int i = 0; i < each; i++) b.put(base + i);
                    } catch (InterruptedException ignored) {
                    }
                }));
            }
            for (int c = 0; c < consumers; c++) {
                cs.add(new Thread(() -> {
                    try {
                        while (true) {
                            int v = b.take();
                            if (v < 0) return;
                            seen.incrementAndGet(v);
                        }
                    } catch (InterruptedException ignored) {
                    }
                }));
            }
            ps.forEach(Thread::start);
            cs.forEach(Thread::start);
            for (Thread t : ps) t.join();
            for (int c = 0; c < consumers; c++) b.put(-1);
            for (Thread t : cs) t.join();
        });
        for (int i = 0; i < total; i++) assertEquals(1, seen.get(i), "item " + i);
    }
}
