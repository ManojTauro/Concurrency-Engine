package dev.conc.p5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/** What every correct store must do, single-threaded and under contention. */
abstract class ProfileStoreContract {

    abstract ProfileStore create();

    @Test
    void unknownUserIsZero() {
        ProfileStore s = create();
        assertEquals(0, s.count("nobody", EventType.VIEW));
        assertEquals(0, s.score("nobody"));
        assertEquals(0, s.totalEvents());
    }

    @Test
    void singleThreadedCounts() {
        ProfileStore s = create();
        List<Event> events = TestEvents.events(2_000, 16);
        events.forEach(s::record);
        assertConsistent(s, events, 16);
    }

    @RepeatedTest(5)
    void concurrentRecordsAreNotLost() throws Exception {
        ProfileStore s = create();
        hammer(s, 8, TestEvents.events(40_000, 64), 64);
    }

    @RepeatedTest(3)
    void readersDuringWritesNeverFail() throws Exception {
        ProfileStore s = create();
        List<Event> events = TestEvents.events(40_000, 64);
        AtomicBoolean done = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread reader = new Thread(() -> {
            try {
                long last = 0;
                while (!done.get()) {
                    for (int u = 0; u < 64; u++) {
                        s.count("user-" + u, EventType.CLICK);
                        s.score("user-" + u);
                    }
                    long total = s.totalEvents();
                    if (total < last) throw new AssertionError("total went backwards: " + last + " -> " + total);
                    last = total;
                }
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        reader.start();
        try {
            hammer(s, 6, events, 64);
        } finally {
            done.set(true);
            reader.join();
        }
        assertNull(failure.get(), () -> "reader failed: " + failure.get());
    }

    /** Record every event from {@code threads} threads, then check every count exactly. */
    static void hammer(ProfileStore s, int threads, List<Event> events, int users) throws Exception {
        List<Thread> ts = new ArrayList<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        int slice = (events.size() + threads - 1) / threads;
        for (int t = 0; t < threads; t++) {
            List<Event> part = events.subList(Math.min(events.size(), t * slice), Math.min(events.size(), (t + 1) * slice));
            Thread th = new Thread(() -> {
                try {
                    part.forEach(s::record);
                } catch (Throwable e) {
                    failure.set(e);
                }
            });
            ts.add(th);
        }
        ts.forEach(Thread::start);
        for (Thread th : ts) th.join();
        assertNull(failure.get(), () -> "a worker failed: " + failure.get());
        assertConsistent(s, events, users);
    }

    private static void assertConsistent(ProfileStore s, List<Event> events, int users) {
        assertEquals(events.size(), s.totalEvents());
        for (int u = 0; u < users; u++) {
            String id = "user-" + u;
            for (EventType type : EventType.values()) {
                assertEquals(TestEvents.expectedCount(events, id, type), s.count(id, type), id + " " + type);
            }
            assertEquals(TestEvents.expectedScore(events, id), s.score(id), id + " score");
        }
    }
}
