package dev.conc.p6;

import java.util.ArrayList;
import java.util.List;

/**
 * Given. The experiment for the results note: the same skewed stream into the naive pool and into
 * your lanes. Run it with: mvn -q compile exec:java is not configured; run the class from your IDE
 * or: java -cp target/classes dev.conc.p6.OrderingDemoMain
 */
public final class OrderingDemoMain {
    public static void main(String[] args) throws Exception {
        int users = 4, n = 400_000;
        long[] seq = new long[users];
        List<Event> events = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int u = (i % 10 < 6) ? 0 : (i % users);
            events.add(new Event(i + 1, "user-" + u, "product-" + (i % 50), EventType.values()[i % 4], i, ++seq[u]));
        }
        System.out.println("naive pool, 8 threads:");
        try (NaivePoolStore naive = new NaivePoolStore(8)) {
            events.forEach(naive::record);
            naive.awaitIdle();
            System.out.println("  applied " + naive.totalEvents() + ", out of order " + naive.orderViolations());
        }
        System.out.println("lane store, 8 lanes:");
        try (LaneProfileStore lanes = new LaneProfileStore(8)) {
            events.forEach(lanes::record);
            lanes.awaitIdle();
            System.out.println("  applied " + lanes.totalEvents() + ", out of order " + lanes.orderViolations());
        } catch (UnsupportedOperationException todo) {
            System.out.println("  not implemented yet (challenge 3)");
        }
    }
}
