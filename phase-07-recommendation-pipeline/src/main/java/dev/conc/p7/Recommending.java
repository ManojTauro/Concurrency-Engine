package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Anything that can recommend products for a user without blocking the caller. */
@FunctionalInterface
public interface Recommending {
    CompletableFuture<List<String>> recommend(String userId);
}
