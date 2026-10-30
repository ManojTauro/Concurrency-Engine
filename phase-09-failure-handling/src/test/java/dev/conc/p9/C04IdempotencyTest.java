package dev.conc.p9;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import org.junit.jupiter.api.Test;

class C04IdempotencyTest {

    private static Event ev(long id) {
        return new Event(id, "u", "p", EventType.VIEW, 0, id);
    }

    @Test
    void repeatedIdsAreNotNewTheSecondTime() {
        DedupFilter f = new DedupFilter(10);
        assertTrue(f.firstTime(1));
        assertTrue(f.firstTime(2));
        assertFalse(f.firstTime(1));
        assertFalse(f.firstTime(2));
        assertEquals(2, f.size());
    }

    @Test
    void theFirstSeenIdIsForgottenWhenFullAndAHitDoesNotRefreshIt() {
        DedupFilter f = new DedupFilter(2);
        assertTrue(f.firstTime(1));
        assertTrue(f.firstTime(2));
        assertFalse(f.firstTime(1));
        assertTrue(f.firstTime(3), "3 is new; 1 (first seen) is forgotten");
        assertEquals(2, f.size());
        assertFalse(f.firstTime(2));
        assertFalse(f.firstTime(3));
        assertTrue(f.firstTime(1), "1 was forgotten");
    }

    @Test
    void forgetMakesAnIdNewAgain() {
        DedupFilter f = new DedupFilter(5);
        f.firstTime(7);
        f.forget(7);
        assertEquals(0, f.size());
        assertTrue(f.firstTime(7));
        f.forget(99);
    }

    @Test
    void memoryStaysBounded() {
        DedupFilter f = new DedupFilter(1_000);
        for (long i = 0; i < 50_000; i++) f.firstTime(i);
        assertEquals(1_000, f.size());
        assertThrows(IllegalArgumentException.class, () -> new DedupFilter(0));
    }

    @Test
    void whenManyThreadsRaceOnOneIdExactlyOneWins() throws Exception {
        for (int round = 0; round < 200; round++) {
            DedupFilter f = new DedupFilter(100);
            AtomicInteger winners = new AtomicInteger();
            List<Thread> ts = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                ts.add(new Thread(() -> {
                    if (f.firstTime(42)) winners.incrementAndGet();
                }));
            }
            ts.forEach(Thread::start);
            for (Thread t : ts) t.join();
            assertEquals(1, winners.get(), "round " + round);
        }
    }

    @Test
    void everyIdIsNewExactlyOnceAcrossThreads() throws Exception {
        int ids = 20_000;
        DedupFilter f = new DedupFilter(ids);
        AtomicIntegerArray wins = new AtomicIntegerArray(ids);
        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            ts.add(new Thread(() -> {
                for (int i = 0; i < ids; i++) if (f.firstTime(i)) wins.incrementAndGet(i);
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        for (int i = 0; i < ids; i++) assertEquals(1, wins.get(i), "id " + i);
    }

    @Test
    void theRecorderAppliesAnEventOnce() {
        List<Long> applied = new ArrayList<>();
        AtMostOnceRecorder r = new AtMostOnceRecorder(new DedupFilter(100), e -> applied.add(e.eventId()));
        assertTrue(r.record(ev(1)));
        assertFalse(r.record(ev(1)));
        assertTrue(r.record(ev(2)));
        assertFalse(r.record(ev(1)));
        assertEquals(List.of(1L, 2L), applied);
    }

    @Test
    void aSinkFailureLeavesTheEventRetryable() {
        AtomicInteger calls = new AtomicInteger();
        List<Long> applied = new ArrayList<>();
        AtMostOnceRecorder r = new AtMostOnceRecorder(new DedupFilter(100), e -> {
            if (calls.incrementAndGet() == 1) throw new IllegalStateException("db down");
            applied.add(e.eventId());
        });
        assertThrows(IllegalStateException.class, () -> r.record(ev(5)));
        assertTrue(r.record(ev(5)), "the retry must go through");
        assertFalse(r.record(ev(5)));
        assertEquals(List.of(5L), applied);
    }

    @Test
    void concurrentRetriesOfTheSameEventsApplyEachOnce() throws Exception {
        int ids = 5_000;
        AtomicIntegerArray applied = new AtomicIntegerArray(ids);
        AtMostOnceRecorder r = new AtMostOnceRecorder(new DedupFilter(ids), e -> applied.incrementAndGet((int) e.eventId()));
        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < 6; t++) {
            ts.add(new Thread(() -> {
                for (int i = 0; i < ids; i++) r.record(ev(i));
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        for (int i = 0; i < ids; i++) assertEquals(1, applied.get(i), "event " + i);
    }
}
