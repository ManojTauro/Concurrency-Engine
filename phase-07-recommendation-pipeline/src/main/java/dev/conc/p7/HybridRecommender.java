package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Challenge 2. Combine two independent sources: the personalised recommendation and the trending
 * products. Both calls start immediately and run at the same time (the total time is about the
 * slower one, not the sum).
 *
 *  - result = the personalised items in order, then trending items that are not already in the
 *    list, cut to {@code size};
 *  - trending is asked for exactly {@code size} items;
 *  - if one source fails, the result is the other one alone (cut to size); if both fail, the
 *    returned future fails.
 */
public final class HybridRecommender implements Recommending {

    public HybridRecommender(Recommending personalised, TrendingClient trending, int size) {
        // TODO
    }

    @Override
    public CompletableFuture<List<String>> recommend(String userId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
