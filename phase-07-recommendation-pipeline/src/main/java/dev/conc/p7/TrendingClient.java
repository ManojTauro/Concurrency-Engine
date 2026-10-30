package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** A remote service: the k currently trending products. */
@FunctionalInterface
public interface TrendingClient {
    CompletableFuture<List<String>> top(int k);
}
