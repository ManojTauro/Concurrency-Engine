package dev.conc.p1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class C02EventGeneratorTest {

    @Test
    void sameSeedGivesSameStream() {
        List<Event> a = new EventGenerator(42, 50, 20, 1.0).take(1000);
        List<Event> b = new EventGenerator(42, 50, 20, 1.0).take(1000);
        assertEquals(a, b);
    }

    @Test
    void differentSeedsGiveDifferentStreams() {
        List<Event> a = new EventGenerator(1, 50, 20, 1.0).take(1000);
        List<Event> b = new EventGenerator(2, 50, 20, 1.0).take(1000);
        assertNotEquals(a, b);
    }

    @Test
    void takeContinuesWhereNextStopped() {
        EventGenerator g = new EventGenerator(7, 10, 10, 0);
        Event first = g.next();
        List<Event> rest = g.take(3);
        assertEquals(1, first.eventId());
        assertEquals(List.of(2L, 3L, 4L), rest.stream().map(Event::eventId).toList());
    }

    @Test
    void eventIdsAreConsecutiveFromOne() {
        List<Event> events = new EventGenerator(3, 20, 5, 0.5).take(500);
        for (int i = 0; i < events.size(); i++) {
            assertEquals(i + 1, events.get(i).eventId());
        }
    }

    @Test
    void timestampsNeverGoBackwards() {
        List<Event> events = new EventGenerator(3, 20, 5, 0.5).take(500);
        for (int i = 1; i < events.size(); i++) {
            assertTrue(events.get(i).timestampMillis() >= events.get(i - 1).timestampMillis());
        }
    }

    @Test
    void perUserSequenceCountsUpWithoutGaps() {
        Map<String, Long> last = new HashMap<>();
        for (Event e : new EventGenerator(11, 30, 5, 1.0).take(5000)) {
            long expected = last.getOrDefault(e.userId(), 0L) + 1;
            assertEquals(expected, e.userSeq(), "user " + e.userId());
            last.put(e.userId(), expected);
        }
    }

    @Test
    void idsStayInRange() {
        for (Event e : new EventGenerator(5, 12, 7, 1.0).take(3000)) {
            int u = Integer.parseInt(e.userId().substring("user-".length()));
            int p = Integer.parseInt(e.productId().substring("product-".length()));
            assertTrue(u >= 0 && u < 12);
            assertTrue(p >= 0 && p < 7);
        }
    }

    @Test
    void allEventTypesAppear() {
        Set<EventType> seen = EnumSet.noneOf(EventType.class);
        new EventGenerator(5, 10, 10, 0).take(10_000).forEach(e -> seen.add(e.type()));
        assertEquals(EnumSet.allOf(EventType.class), seen);
    }

    @Test
    void zeroSkewSpreadsLoadEvenly() {
        Map<String, Integer> counts = new HashMap<>();
        new EventGenerator(9, 100, 10, 0).take(100_000).forEach(e -> counts.merge(e.userId(), 1, Integer::sum));
        int max = Collections.max(counts.values());
        assertEquals(100, counts.size());
        assertTrue(max < 1300, "busiest user had " + max + " of 100000 events");
    }

    @Test
    void positiveSkewMakesUserZeroHot() {
        Map<String, Integer> counts = new HashMap<>();
        new EventGenerator(9, 100, 10, 1.2).take(100_000).forEach(e -> counts.merge(e.userId(), 1, Integer::sum));
        int hot = counts.getOrDefault("user-0", 0);
        assertTrue(hot > 10_000, "user-0 had only " + hot + " of 100000 events");
        assertEquals(hot, Collections.max(counts.values()));
    }

    @Test
    void badArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EventGenerator(1, 0, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> new EventGenerator(1, 5, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EventGenerator(1, 5, 5, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new EventGenerator(1, 5, 5, 0).take(-1));
        assertTrue(new EventGenerator(1, 5, 5, 0).take(0).isEmpty());
    }
}
