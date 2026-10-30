package dev.conc.p8;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class C02BoundedPoliciesTest {

    private static Event ev(long id) {
        return new Event(id, "u", "p", EventType.VIEW, 0, id);
    }

    @Test
    void capacityBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BoundedIngest(0, Policy.BLOCK, Duration.ZERO));
    }

    @Test
    void everyPolicyAcceptsWhileThereIsRoomAndIsFifo() throws Exception {
        for (Policy p : Policy.values()) {
            BoundedIngest q = new BoundedIngest(3, p, Duration.ofMillis(50));
            for (int i = 1; i <= 3; i++) assertEquals(Outcome.ACCEPTED, q.submit(ev(i)), p.name());
            assertEquals(3, q.size());
            for (int i = 1; i <= 3; i++) assertEquals(i, q.take().eventId(), p.name());
            assertEquals(0, q.rejected());
            assertEquals(0, q.droppedOldest());
        }
    }

    @Test
    void blockWaitsForSpaceAndIsInterruptible() throws Exception {
        BoundedIngest q = new BoundedIngest(1, Policy.BLOCK, Duration.ZERO);
        q.submit(ev(1));
        AtomicReference<Outcome> outcome = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try {
                outcome.set(q.submit(ev(2)));
            } catch (InterruptedException ignored) {
            }
        });
        t.start();
        Thread.sleep(150);
        assertTrue(t.isAlive());
        assertEquals(1, q.take().eventId());
        t.join(2000);
        assertEquals(Outcome.ACCEPTED, outcome.get());

        AtomicInteger interrupted = new AtomicInteger();
        Thread t2 = new Thread(() -> {
            try {
                q.submit(ev(3));
            } catch (InterruptedException e) {
                interrupted.set(1);
            }
        });
        t2.start();
        Thread.sleep(100);
        t2.interrupt();
        t2.join(2000);
        assertEquals(1, interrupted.get());
        assertEquals(1, q.size());
    }

    @Test
    void rejectNewRefusesImmediately() throws Exception {
        BoundedIngest q = new BoundedIngest(2, Policy.REJECT_NEW, Duration.ZERO);
        q.submit(ev(1));
        q.submit(ev(2));
        long t0 = System.nanoTime();
        assertEquals(Outcome.REJECTED, q.submit(ev(3)));
        assertEquals(Outcome.REJECTED, q.submit(ev(4)));
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 100);
        assertEquals(2, q.rejected());
        assertEquals(1, q.take().eventId());
        assertEquals(2, q.take().eventId());
    }

    @Test
    void dropOldestKeepsTheNewestEvents() throws Exception {
        BoundedIngest q = new BoundedIngest(3, Policy.DROP_OLDEST, Duration.ZERO);
        for (int i = 1; i <= 3; i++) q.submit(ev(i));
        assertEquals(Outcome.ACCEPTED_DROPPED_OLDEST, q.submit(ev(4)));
        assertEquals(Outcome.ACCEPTED_DROPPED_OLDEST, q.submit(ev(5)));
        assertEquals(2, q.droppedOldest());
        assertEquals(0, q.rejected());
        assertEquals(3, q.size());
        assertEquals(3, q.take().eventId());
        assertEquals(4, q.take().eventId());
        assertEquals(5, q.take().eventId());
    }

    @Test
    void timeoutRefusesAfterWaitingAndAcceptsIfSpaceAppears() throws Exception {
        BoundedIngest q = new BoundedIngest(1, Policy.TIMEOUT, Duration.ofMillis(150));
        q.submit(ev(1));
        long t0 = System.nanoTime();
        assertEquals(Outcome.REJECTED, q.submit(ev(2)));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms >= 130 && ms < 1000, "waited " + ms + " ms");
        assertEquals(1, q.rejected());

        Thread freer = new Thread(() -> {
            try {
                Thread.sleep(50);
                q.take();
            } catch (InterruptedException ignored) {
            }
        });
        freer.start();
        assertEquals(Outcome.ACCEPTED, q.submit(ev(3)));
        freer.join();
    }

    @Test
    void underContentionTheCapacityIsNeverExceededAndNothingIsUnaccounted() throws Exception {
        int producers = 8, each = 10_000, capacity = 100;
        BoundedIngest reject = new BoundedIngest(capacity, Policy.REJECT_NEW, Duration.ZERO);
        BoundedIngest drop = new BoundedIngest(capacity, Policy.DROP_OLDEST, Duration.ZERO);
        AtomicInteger accepted = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            int base = p * each;
            ts.add(new Thread(() -> {
                try {
                    for (int i = 0; i < each; i++) {
                        if (reject.submit(ev(base + i)) == Outcome.ACCEPTED) accepted.incrementAndGet();
                        drop.submit(ev(base + i));
                    }
                } catch (InterruptedException ignored) {
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(capacity, reject.size());
        assertEquals(capacity, accepted.get());
        assertEquals(producers * each - capacity, reject.rejected());
        assertEquals(capacity, drop.size());
        assertEquals(producers * each - capacity, drop.droppedOldest());
    }
}
