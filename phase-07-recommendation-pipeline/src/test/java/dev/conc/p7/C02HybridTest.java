package dev.conc.p7;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C02HybridTest {

    private static List<String> get(CompletableFuture<List<String>> f) throws Exception {
        return f.get(10, TimeUnit.SECONDS);
    }

    private static Recommending fixed(String... items) {
        return u -> CompletableFuture.completedFuture(List.of(items));
    }

    private static TrendingClient trendingOf(String... items) {
        return k -> CompletableFuture.completedFuture(List.of(items));
    }

    @Test
    void personalisedFirstThenTrendingWithoutDuplicates() throws Exception {
        HybridRecommender h = new HybridRecommender(fixed("p1", "p2"), trendingOf("p2", "t1", "t2", "t3"), 5);
        assertEquals(List.of("p1", "p2", "t1", "t2", "t3"), get(h.recommend("u")));
    }

    @Test
    void theResultIsCutToSize() throws Exception {
        HybridRecommender h = new HybridRecommender(fixed("p1", "p2", "p3"), trendingOf("t1", "t2"), 4);
        assertEquals(List.of("p1", "p2", "p3", "t1"), get(h.recommend("u")));
    }

    @Test
    void trendingIsAskedForExactlySizeItems() throws Exception {
        AtomicInteger asked = new AtomicInteger();
        HybridRecommender h = new HybridRecommender(fixed("p1"), k -> {
            asked.set(k);
            return CompletableFuture.completedFuture(List.of("t1"));
        }, 7);
        get(h.recommend("u"));
        assertEquals(7, asked.get());
    }

    @Test
    void bothSourcesRunAtTheSameTime() throws Exception {
        Recommending slowPersonal = u -> Simulator.after(150, () -> List.of("p1"));
        TrendingClient slowTrending = k -> Simulator.after(120, () -> List.of("t1"));
        HybridRecommender h = new HybridRecommender(slowPersonal, slowTrending, 5);
        long t0 = System.nanoTime();
        assertEquals(List.of("p1", "t1"), get(h.recommend("u")));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms < 210, "took " + ms + " ms; one after the other would be about 270 ms");
    }

    @Test
    void returnsWithoutBlocking() throws Exception {
        Recommending slow = u -> Simulator.after(100, () -> List.of("p1"));
        HybridRecommender h = new HybridRecommender(slow, k -> Simulator.after(100, () -> List.of("t1")), 5);
        long t0 = System.nanoTime();
        CompletableFuture<List<String>> f = h.recommend("u");
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 40);
        get(f);
    }

    @Test
    void whenTrendingFailsThePersonalisedListAloneIsReturned() throws Exception {
        HybridRecommender h = new HybridRecommender(fixed("p1", "p2"),
                k -> CompletableFuture.failedFuture(new IllegalStateException("trending down")), 5);
        assertEquals(List.of("p1", "p2"), get(h.recommend("u")));
    }

    @Test
    void whenPersonalisedFailsTrendingAloneIsReturned() throws Exception {
        HybridRecommender h = new HybridRecommender(u -> CompletableFuture.failedFuture(new IllegalStateException("down")),
                trendingOf("t1", "t2", "t3"), 2);
        assertEquals(List.of("t1", "t2"), get(h.recommend("u")));
    }

    @Test
    void whenBothFailTheResultFails() {
        HybridRecommender h = new HybridRecommender(u -> CompletableFuture.failedFuture(new IllegalStateException("a")),
                k -> CompletableFuture.failedFuture(new IllegalStateException("b")), 3);
        assertThrows(ExecutionException.class, () -> h.recommend("u").get(10, TimeUnit.SECONDS));
    }

    @Test
    void aSourceThatThrowsWhenCalledIsTreatedAsAFailure() throws Exception {
        HybridRecommender h = new HybridRecommender(u -> {
            throw new IllegalStateException("sync failure");
        }, trendingOf("t1"), 3);
        assertEquals(List.of("t1"), get(h.recommend("u")));
    }
}
