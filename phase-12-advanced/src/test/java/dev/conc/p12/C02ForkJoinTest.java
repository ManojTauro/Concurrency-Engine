package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.Test;

class C02ForkJoinTest {

    private static List<Event> events(int n, int users) {
        List<Event> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new Event(i + 1, "user-" + ((i * 7 + i / 3) % users), "p" + (i % 50), EventType.values()[(i + i / users) % 4], i, i));
        }
        return list;
    }

    private static Map<String, Long> sequential(List<Event> events) {
        Map<String, Long> m = new HashMap<>();
        for (Event e : events) m.merge(e.userId(), (long) e.type().weight(), Long::sum);
        return m;
    }

    @Test
    void matchesTheSequentialResult() {
        List<Event> events = events(200_000, 1_000);
        ForkJoinPool pool = new ForkJoinPool(4);
        try {
            assertEquals(sequential(events), ParallelScorer.scoreByUser(events, pool, 1_000));
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void worksForAnyThresholdIncludingOneBiggerThanTheInput() {
        List<Event> events = events(5_000, 37);
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            for (int threshold : new int[]{1, 7, 100, 5_000, 1_000_000}) {
                assertEquals(sequential(events), ParallelScorer.scoreByUser(events, pool, threshold), "threshold " + threshold);
            }
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void theWorkRunsInTheGivenPool() {
        ForkJoinPool pool = new ForkJoinPool(4);
        try {
            assertEquals(0, pool.getPoolSize());
            ParallelScorer.scoreByUser(events(50_000, 100), pool, 500);
            assertTrue(pool.getPoolSize() > 0, "no worker of the given pool was used");
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void emptyInputAndBadThreshold() {
        ForkJoinPool pool = new ForkJoinPool(2);
        try {
            assertEquals(Map.of(), ParallelScorer.scoreByUser(List.of(), pool, 10));
            assertThrows(IllegalArgumentException.class, () -> ParallelScorer.scoreByUser(events(10, 2), pool, 0));
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void theInputListIsNotModified() {
        List<Event> events = events(2_000, 20);
        List<Event> copy = new ArrayList<>(events);
        ForkJoinPool pool = new ForkJoinPool(4);
        try {
            ParallelScorer.scoreByUser(events, pool, 100);
        } finally {
            pool.shutdown();
        }
        assertEquals(copy, events);
    }
}
