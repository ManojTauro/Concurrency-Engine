package dev.conc.p7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Given. Deterministic fake services with a fixed latency, built on Simulator. */
public final class SimClients {
    private SimClients() {
    }

    /** "user-N" has recently touched product-N .. product-(N+3). */
    public static ProfileClient profile(long latencyMillis) {
        return userId -> Simulator.after(latencyMillis, () -> {
            int n = Integer.parseInt(userId.substring(userId.indexOf('-') + 1));
            return List.of("product-" + n, "product-" + (n + 1), "product-" + (n + 2), "product-" + (n + 3));
        });
    }

    /** "product-K" is similar to product-(K+10), product-(K+11), product-(K+12). */
    public static CatalogClient catalog(long latencyMillis) {
        return productId -> Simulator.after(latencyMillis, () -> {
            int k = Integer.parseInt(productId.substring(productId.indexOf('-') + 1));
            return List.of("product-" + (k + 10), "product-" + (k + 11), "product-" + (k + 12));
        });
    }

    public static TrendingClient trending(long latencyMillis) {
        return k -> Simulator.after(latencyMillis, () -> {
            List<String> out = new ArrayList<>();
            for (int i = 0; i < k; i++) out.add("product-" + (900 + i));
            return out;
        });
    }

    public static ScoringClient scoring(long latencyMillis) {
        return (userId, candidates) -> Simulator.after(latencyMillis, () -> {
            Map<String, Double> scores = new HashMap<>();
            for (String c : candidates) scores.put(c, scoreOf(userId, c));
            return scores;
        });
    }

    /** The score the fake scoring service gives; tests use it to compute the expected ranking. */
    public static double scoreOf(String userId, String productId) {
        return Math.abs((userId + "|" + productId).hashCode() % 1000) / 10.0;
    }
}
