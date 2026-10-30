package dev.conc.p7;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** A remote service: a score for each candidate product for this user (higher is better). */
@FunctionalInterface
public interface ScoringClient {
    CompletableFuture<Map<String, Double>> score(String userId, List<String> candidates);
}
