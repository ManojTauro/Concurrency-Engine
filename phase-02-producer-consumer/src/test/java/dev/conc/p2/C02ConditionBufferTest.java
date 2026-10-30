package dev.conc.p2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class C02ConditionBufferTest extends BufferContract {
    @Override
    Buffer<Integer> create(int capacity) {
        return new ConditionBuffer<>(capacity);
    }

    @Test
    void pollTimesOutOnEmpty() throws Exception {
        ConditionBuffer<Integer> b = new ConditionBuffer<>(2);
        long t0 = System.nanoTime();
        assertNull(b.poll(120, TimeUnit.MILLISECONDS));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms >= 100 && ms < 1500, "waited " + ms + " ms");
    }

    @Test
    void offerTimesOutOnFull() throws Exception {
        ConditionBuffer<Integer> b = new ConditionBuffer<>(1);
        b.put(1);
        long t0 = System.nanoTime();
        assertFalse(b.offer(2, 120, TimeUnit.MILLISECONDS));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms >= 100 && ms < 1500, "waited " + ms + " ms");
        assertEquals(1, b.size());
    }

    @Test
    void pollReturnsAnItemThatArrivesInTime() throws Exception {
        ConditionBuffer<Integer> b = new ConditionBuffer<>(2);
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(80);
                b.put(42);
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        long t0 = System.nanoTime();
        assertEquals(42, b.poll(3, TimeUnit.SECONDS));
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 1500);
        t.join();
    }

    @Test
    void offerSucceedsWhenSpaceFreesInTime() throws Exception {
        ConditionBuffer<Integer> b = new ConditionBuffer<>(1);
        b.put(1);
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(80);
                b.take();
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        assertTrue(b.offer(2, 3, TimeUnit.SECONDS));
        t.join();
        assertEquals(1, b.size());
    }

    @Test
    void immediateOperationsDoNotWait() throws Exception {
        ConditionBuffer<Integer> b = new ConditionBuffer<>(1);
        assertTrue(b.offer(1, 0, TimeUnit.MILLISECONDS));
        assertFalse(b.offer(2, 0, TimeUnit.MILLISECONDS));
        assertEquals(1, b.poll(0, TimeUnit.MILLISECONDS));
        assertNull(b.poll(0, TimeUnit.MILLISECONDS));
    }
}
