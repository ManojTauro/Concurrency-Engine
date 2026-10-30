package dev.conc.p7;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Challenge 3. Never make the caller wait for a slow or broken service.
 *
 *  - ask the primary; if it answers within the timeout, that is the result and the fallback is
 *    never called;
 *  - if the primary fails, or does not answer in time, ask the fallback and use its answer;
 *  - if the fallback also fails (or throws when called), the result is an empty list;
 *  - the returned future never completes exceptionally, and a primary that answers late is ignored;
 *  - a primary that throws when called (instead of returning a failed future) counts as a failure.
 */
public final class ResilientRecommender implements Recommending {

    public ResilientRecommender(Recommending primary, Recommending fallback, Duration timeout) {
        // TODO
    }

    @Override
    public CompletableFuture<List<String>> recommend(String userId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
