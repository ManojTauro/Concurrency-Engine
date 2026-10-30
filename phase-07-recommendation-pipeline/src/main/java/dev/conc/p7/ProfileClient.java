package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** A remote service: the products a user touched recently. */
@FunctionalInterface
public interface ProfileClient {
    CompletableFuture<List<String>> recentProducts(String userId);
}
