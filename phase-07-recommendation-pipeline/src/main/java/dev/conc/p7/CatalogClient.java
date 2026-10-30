package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** A remote service: products similar to a product. */
@FunctionalInterface
public interface CatalogClient {
    CompletableFuture<List<String>> similarTo(String productId);
}
