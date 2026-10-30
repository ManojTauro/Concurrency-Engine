package dev.conc.p6;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class C03LaneProfileStoreTest {

    /** Events with a per-user userSeq that rises along the list. */
    private static List<Event> events(int n, int users) {
        long[] seq = new long[users];
        List<Event> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int u = (i * 7 + i / 3) % users;
            list.add(new Event(i + 1, "user-" + u, "product-" + (i % 50), EventType.values()[(i + u) % 4], i, ++seq[u]));
        }
        return list;
    }

    private static long expectedCount(List<Event> events, String user, EventType type) {
        return events.stream().filter(e -> e.userId().equals(user) && e.type() == type).count();
    }

    private static long expectedScore(List<Event> events, String user) {
        return events.stream().filter(e -> e.userId().equals(user)).mapToLong(e -> e.type().weight()).sum();
    }

    private static void assertExact(OrderedStore s, List<Event> events, int users) {
        assertEquals(events.size(), s.totalEvents());
        for (int u = 0; u < users; u++) {
            String id = "user-" + u;
            for (EventType type : EventType.values()) assertEquals(expectedCount(events, id, type), s.count(id, type), id + " " + type);
            assertEquals(expectedScore(events, id), s.score(id), id + " score");
        }
    }

    @Test
    void oneSubmitterGivesExactCountsAndNoViolations() throws Exception {
        List<Event> events = events(40_000, 64);
        try (OrderedStore s = new LaneProfileStore(8)) {
            events.forEach(s::record);
            s.awaitIdle();
            assertExact(s, events, 64);
            assertEquals(0, s.orderViolations());
        }
    }

    @Test
    void manySubmittersWithDisjointUsersKeepEveryUsersOrder() throws Exception {
        List<Event> events = events(40_000, 64);
        try (OrderedStore s = new LaneProfileStore(8)) {
            assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
                List<Thread> ts = new ArrayList<>();
                for (int t = 0; t < 8; t++) {
                    int id = t;
                    ts.add(new Thread(() -> {
                        for (Event e : events) if (Integer.parseInt(e.userId().substring(5)) % 8 == id) s.record(e);
                    }));
                }
                ts.forEach(Thread::start);
                for (Thread t : ts) t.join();
                s.awaitIdle();
            });
            assertExact(s, events, 64);
            assertEquals(0, s.orderViolations());
        }
    }

    @Test
    void outOfOrderEventsForAUserAreDetected() throws Exception {
        try (OrderedStore s = new LaneProfileStore(2)) {
            s.record(new Event(1, "u", "p", EventType.VIEW, 0, 5));
            s.record(new Event(2, "u", "p", EventType.VIEW, 0, 3));
            s.record(new Event(3, "u", "p", EventType.VIEW, 0, 6));
            s.record(new Event(4, "u", "p", EventType.VIEW, 0, 6));
            s.awaitIdle();
            assertEquals(2, s.orderViolations());
            assertEquals(4, s.totalEvents());
        }
    }

    @Test
    void worksWithOneLaneAndWithMoreLanesThanUsers() throws Exception {
        List<Event> events = events(5_000, 5);
        for (int lanes : new int[]{1, 32}) {
            try (OrderedStore s = new LaneProfileStore(lanes)) {
                events.forEach(s::record);
                s.awaitIdle();
                assertExact(s, events, 5);
            }
        }
    }

    @Test
    void unknownUsersReadAsZero() throws Exception {
        try (OrderedStore s = new LaneProfileStore(4)) {
            assertEquals(0, s.count("nobody", EventType.CLICK));
            assertEquals(0, s.score("nobody"));
            assertEquals(0, s.totalEvents());
        }
    }

    @Test
    void readersDuringWritesSeeMonotonicCounts() throws Exception {
        List<Event> events = events(40_000, 16);
        try (OrderedStore s = new LaneProfileStore(4)) {
            AtomicBoolean done = new AtomicBoolean();
            AtomicReference<String> failure = new AtomicReference<>();
            Thread reader = new Thread(() -> {
                long lastScore = 0;
                while (!done.get()) {
                    long sc = s.score("user-3");
                    if (sc < lastScore) failure.set("score went backwards: " + lastScore + " -> " + sc);
                    lastScore = sc;
                }
            });
            reader.start();
            events.forEach(s::record);
            s.awaitIdle();
            done.set(true);
            reader.join();
            assertNull(failure.get());
            assertExact(s, events, 16);
        }
    }

    @Test
    void recordAfterCloseIsRejected() {
        OrderedStore s = new LaneProfileStore(2);
        s.close();
        assertThrows(RejectedExecutionException.class, () -> s.record(new Event(1, "u", "p", EventType.VIEW, 0, 1)));
    }
}
