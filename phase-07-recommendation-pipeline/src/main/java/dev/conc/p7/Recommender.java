package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Challenge 1. Build the personal recommendation as a chain of futures, never blocking a thread:
 *  1. recent = profile.recentProducts(userId);
 *  2. for EVERY recent product, ask catalog.similarTo(product), all calls in flight at the same time;
 *  3. candidates = the distinct similar products in order of first appearance, without any product
 *     in recent;
 *  4. scores = scoring.score(userId, candidates), called exactly once, and only if there are candidates;
 *  5. the answer is the (up to) 5 best candidates, highest score first, ties by product id ascending.
 *
 * An empty history completes with an empty list. If any call fails, the returned future fails (the
 * failure of that call is its cause). recommend() itself returns at once; it must not call join()
 * or get().
 */
public final class Recommender implements Recommending {

    public Recommender(ProfileClient profile, CatalogClient catalog, ScoringClient scoring) {
        // TODO
    }

    @Override
    public CompletableFuture<List<String>> recommend(String userId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
