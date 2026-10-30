package dev.conc.p5;

import java.util.ArrayList;
import java.util.List;

/** Test helper: a known stream, so the right answers can be computed independently. */
final class TestEvents {
    private TestEvents() {
    }

    static List<Event> events(int n, int users) {
        long[] seq = new long[users];
        List<Event> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int u = (i * 7 + i / 3) % users;
            list.add(new Event(i + 1, "user-" + u, "product-" + (i % 50), EventType.values()[(i + u) % 4], i, ++seq[u]));
        }
        return list;
    }

    static long expectedCount(List<Event> events, String user, EventType type) {
        return events.stream().filter(e -> e.userId().equals(user) && e.type() == type).count();
    }

    static long expectedScore(List<Event> events, String user) {
        return events.stream().filter(e -> e.userId().equals(user)).mapToLong(e -> e.type().weight()).sum();
    }
}
