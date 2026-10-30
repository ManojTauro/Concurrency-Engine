package dev.conc.p7;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C01RecommenderTest {

    private static List<String> get(CompletableFuture<List<String>> f) throws Exception {
        return f.get(10, TimeUnit.SECONDS);
    }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }

    @Test
    void ranksTheTopFiveCandidatesByScore() throws Exception {
        Recommender r = new Recommender(SimClients.profile(20), SimClients.catalog(20), SimClients.scoring(20));
        // user-1 touched product-1..4, so the similar ones are product-11..16
        List<String> expected = new ArrayList<>(List.of("product-11", "product-12", "product-13", "product-14", "product-15", "product-16"));
        expected.sort(Comparator.<String>comparingDouble(p -> -SimClients.scoreOf("user-1", p)).thenComparing(Comparator.naturalOrder()));
        assertEquals(expected.subList(0, 5), get(r.recommend("user-1")));
    }

    @Test
    void similarProductCallsRunAtTheSameTime() throws Exception {
        Recommender r = new Recommender(SimClients.profile(50), SimClients.catalog(60), SimClients.scoring(30));
        long t0 = System.nanoTime();
        get(r.recommend("user-1"));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        // one after another would be 50 + 4 * 60 + 30 = 320 ms; in parallel about 140 ms
        assertTrue(ms < 230, "took " + ms + " ms");
    }

    @Test
    void recommendReturnsImmediatelyWithoutBlocking() throws Exception {
        Recommender r = new Recommender(SimClients.profile(100), SimClients.catalog(100), SimClients.scoring(100));
        long t0 = System.nanoTime();
        CompletableFuture<List<String>> f = r.recommend("user-1");
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms < 40, "recommend() blocked for " + ms + " ms");
        assertFalse(f.isDone());
        get(f);
    }

    @Test
    void recentProductsAndDuplicatesAreLeftOut() throws Exception {
        List<List<String>> scored = Collections.synchronizedList(new ArrayList<>());
        Recommender r = new Recommender(
                u -> CompletableFuture.completedFuture(List.of("a", "b")),
                p -> CompletableFuture.completedFuture(p.equals("a") ? List.of("b", "x", "y") : List.of("x", "z", "a")),
                (u, cands) -> {
                    scored.add(cands);
                    Map<String, Double> m = new java.util.HashMap<>();
                    cands.forEach(c -> m.put(c, 1.0));
                    return CompletableFuture.completedFuture(m);
                });
        assertEquals(List.of("x", "y", "z"), get(r.recommend("u")));
        assertEquals(1, scored.size(), "scoring must be called once");
        assertEquals(List.of("x", "y", "z"), scored.get(0), "candidates: distinct, first-seen order");
    }

    @Test
    void anEmptyHistoryGivesAnEmptyListWithoutScoring() throws Exception {
        AtomicInteger scoringCalls = new AtomicInteger();
        Recommender r = new Recommender(
                u -> CompletableFuture.completedFuture(List.of()),
                p -> CompletableFuture.completedFuture(List.of("x")),
                (u, c) -> {
                    scoringCalls.incrementAndGet();
                    return CompletableFuture.completedFuture(Map.of());
                });
        assertEquals(List.of(), get(r.recommend("u")));
        assertEquals(0, scoringCalls.get());
    }

    @Test
    void noCandidatesMeansNoScoringCall() throws Exception {
        AtomicInteger scoringCalls = new AtomicInteger();
        Recommender r = new Recommender(
                u -> CompletableFuture.completedFuture(List.of("a")),
                p -> CompletableFuture.completedFuture(List.of("a")),
                (u, c) -> {
                    scoringCalls.incrementAndGet();
                    return CompletableFuture.completedFuture(Map.of());
                });
        assertEquals(List.of(), get(r.recommend("u")));
        assertEquals(0, scoringCalls.get());
    }

    @Test
    void aFailingCatalogCallFailsTheResultWithThatCause() {
        Recommender r = new Recommender(
                u -> CompletableFuture.completedFuture(List.of("a", "b", "c")),
                p -> p.equals("b") ? CompletableFuture.failedFuture(new IllegalStateException("catalog down")) : CompletableFuture.completedFuture(List.of("x")),
                (u, c) -> CompletableFuture.completedFuture(Map.of()));
        ExecutionException ex = assertThrows(ExecutionException.class, () -> r.recommend("u").get(10, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, rootCause(ex));
        assertEquals("catalog down", rootCause(ex).getMessage());
    }

    @Test
    void manyRequestsAtOnceAllFinishWithTheirOwnAnswers() throws Exception {
        Recommender r = new Recommender(SimClients.profile(10), SimClients.catalog(10), SimClients.scoring(10));
        List<CompletableFuture<List<String>>> fs = new ArrayList<>();
        for (int u = 0; u < 200; u++) fs.add(r.recommend("user-" + (u * 20)));
        Set<List<String>> distinct = new HashSet<>();
        for (CompletableFuture<List<String>> f : fs) {
            List<String> out = get(f);
            assertEquals(5, out.size());
            distinct.add(out);
        }
        assertTrue(distinct.size() > 100, "answers should differ per user");
    }
}
