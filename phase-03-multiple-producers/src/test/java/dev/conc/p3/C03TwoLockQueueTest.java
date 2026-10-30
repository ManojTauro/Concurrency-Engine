package dev.conc.p3;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class C03TwoLockQueueTest {

    @Test
    void emptyPollReturnsNullAndNullOfferIsRejected() {
        TwoLockQueue<Integer> q = new TwoLockQueue<>();
        assertNull(q.poll());
        assertEquals(0, q.size());
        assertThrows(NullPointerException.class, () -> q.offer(null));
    }

    @Test
    void fifoOrderAndSize() {
        TwoLockQueue<Integer> q = new TwoLockQueue<>();
        for (int i = 0; i < 1000; i++) q.offer(i);
        assertEquals(1000, q.size());
        for (int i = 0; i < 1000; i++) assertEquals(i, q.poll());
        assertNull(q.poll());
        assertEquals(0, q.size());
    }

    @Test
    void canBeReusedAfterBecomingEmpty() {
        TwoLockQueue<Integer> q = new TwoLockQueue<>();
        for (int round = 0; round < 100; round++) {
            q.offer(round);
            assertEquals(round, q.poll());
            assertNull(q.poll());
        }
    }

    @RepeatedTest(5)
    void manyProducersManyConsumersDeliverEachItemOnce() {
        TwoLockQueue<Integer> q = new TwoLockQueue<>();
        int producers = 4, consumers = 4, each = 50_000, total = producers * each;
        AtomicIntegerArray seen = new AtomicIntegerArray(total);
        AtomicBoolean producersDone = new AtomicBoolean();
        assertTimeoutPreemptively(Duration.ofSeconds(60), () -> {
            List<Thread> ps = new ArrayList<>(), cs = new ArrayList<>();
            for (int p = 0; p < producers; p++) {
                int base = p * each;
                ps.add(new Thread(() -> {
                    for (int i = 0; i < each; i++) q.offer(base + i);
                }));
            }
            for (int c = 0; c < consumers; c++) {
                cs.add(new Thread(() -> {
                    while (true) {
                        Integer v = q.poll();
                        if (v != null) {
                            seen.incrementAndGet(v);
                        } else if (producersDone.get() && q.size() == 0) {
                            return;
                        } else {
                            Thread.onSpinWait();
                        }
                    }
                }));
            }
            ps.forEach(Thread::start);
            cs.forEach(Thread::start);
            for (Thread t : ps) t.join();
            producersDone.set(true);
            for (Thread t : cs) t.join();
        });
        for (int i = 0; i < total; i++) assertEquals(1, seen.get(i), "item " + i);
        assertEquals(0, q.size());
    }

    @RepeatedTest(3)
    void itemsOfOneProducerComeOutInOrder() {
        TwoLockQueue<long[]> q = new TwoLockQueue<>();
        int producers = 4, each = 30_000;
        assertTimeoutPreemptively(Duration.ofSeconds(60), () -> {
            List<Thread> ps = new ArrayList<>();
            for (int p = 0; p < producers; p++) {
                int id = p;
                ps.add(new Thread(() -> {
                    for (int i = 0; i < each; i++) q.offer(new long[]{id, i});
                }));
            }
            long[] last = new long[producers];
            java.util.Arrays.fill(last, -1);
            AtomicBoolean consumerFailed = new AtomicBoolean();
            Thread consumer = new Thread(() -> {
                int got = 0;
                while (got < producers * each) {
                    long[] v = q.poll();
                    if (v == null) {
                        Thread.onSpinWait();
                        continue;
                    }
                    if (v[1] != last[(int) v[0]] + 1) consumerFailed.set(true);
                    last[(int) v[0]] = v[1];
                    got++;
                }
            });
            ps.forEach(Thread::start);
            consumer.start();
            for (Thread t : ps) t.join();
            consumer.join();
            assertFalse(consumerFailed.get(), "an item overtook an earlier item of the same producer");
        });
    }

    @Test
    void sizeNeverGoesNegativeWhileRunning() {
        TwoLockQueue<Integer> q = new TwoLockQueue<>();
        AtomicBoolean stop = new AtomicBoolean();
        AtomicBoolean negative = new AtomicBoolean();
        Thread watcher = new Thread(() -> {
            while (!stop.get()) if (q.size() < 0) negative.set(true);
        });
        Thread producer = new Thread(() -> {
            for (int i = 0; i < 200_000; i++) q.offer(i);
        });
        Thread consumer = new Thread(() -> {
            int got = 0;
            while (got < 200_000) if (q.poll() != null) got++;
        });
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            watcher.start();
            producer.start();
            consumer.start();
            producer.join();
            consumer.join();
            stop.set(true);
            watcher.join();
        });
        assertFalse(negative.get());
    }
}
