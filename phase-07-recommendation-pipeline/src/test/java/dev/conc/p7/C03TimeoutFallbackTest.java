package dev.conc.p7;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C03TimeoutFallbackTest {

    private static List<String> get(CompletableFuture<List<String>> f) throws Exception {
        return f.get(10, TimeUnit.SECONDS);
    }

    @Test
    void aFastPrimaryWinsAndTheFallbackIsNotCalled() throws Exception {
        AtomicInteger fallbackCalls = new AtomicInteger();
        ResilientRecommender r = new ResilientRecommender(
                u -> Simulator.after(20, () -> List.of("primary")),
                u -> {
                    fallbackCalls.incrementAndGet();
                    return CompletableFuture.completedFuture(List.of("fallback"));
                }, Duration.ofMillis(300));
        assertEquals(List.of("primary"), get(r.recommend("u")));
        assertEquals(0, fallbackCalls.get());
    }

    @Test
    void aSlowPrimaryTimesOutAndTheFallbackAnswers() throws Exception {
        ResilientRecommender r = new ResilientRecommender(
                u -> Simulator.after(2_000, () -> List.of("primary")),
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofMillis(100));
        long t0 = System.nanoTime();
        assertEquals(List.of("fallback"), get(r.recommend("u")));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms >= 90 && ms < 600, "took " + ms + " ms");
    }

    @Test
    void aPrimaryThatNeverAnswersDoesNotHangTheCaller() throws Exception {
        ResilientRecommender r = new ResilientRecommender(
                u -> new CompletableFuture<>(),
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofMillis(50));
        assertEquals(List.of("fallback"), get(r.recommend("u")));
    }

    @Test
    void aFailingPrimaryFallsBackImmediately() throws Exception {
        ResilientRecommender r = new ResilientRecommender(
                u -> CompletableFuture.failedFuture(new IllegalStateException("down")),
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofSeconds(5));
        long t0 = System.nanoTime();
        assertEquals(List.of("fallback"), get(r.recommend("u")));
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 500, "must not wait for the timeout");
    }

    @Test
    void aPrimaryThatThrowsWhenCalledFallsBack() throws Exception {
        ResilientRecommender r = new ResilientRecommender(
                u -> {
                    throw new IllegalStateException("sync");
                },
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofSeconds(5));
        assertEquals(List.of("fallback"), get(r.recommend("u")));
    }

    @Test
    void whenBothFailTheResultIsEmptyNotAnError() throws Exception {
        ResilientRecommender r = new ResilientRecommender(
                u -> CompletableFuture.failedFuture(new IllegalStateException("a")),
                u -> CompletableFuture.failedFuture(new IllegalStateException("b")), Duration.ofSeconds(5));
        assertEquals(List.of(), get(r.recommend("u")));
        ResilientRecommender thrower = new ResilientRecommender(
                u -> new CompletableFuture<>(),
                u -> {
                    throw new IllegalStateException("fallback sync");
                }, Duration.ofMillis(50));
        assertEquals(List.of(), get(thrower.recommend("u")));
    }

    @Test
    void aLateFirstAnswerIsIgnored() throws Exception {
        CompletableFuture<List<String>> late = new CompletableFuture<>();
        ResilientRecommender r = new ResilientRecommender(u -> late,
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofMillis(50));
        CompletableFuture<List<String>> f = r.recommend("u");
        assertEquals(List.of("fallback"), get(f));
        late.complete(List.of("primary"));
        assertEquals(List.of("fallback"), get(f));
    }

    @Test
    void returnsWithoutBlocking() {
        ResilientRecommender r = new ResilientRecommender(u -> new CompletableFuture<>(),
                u -> CompletableFuture.completedFuture(List.of("fallback")), Duration.ofSeconds(30));
        long t0 = System.nanoTime();
        CompletableFuture<List<String>> f = r.recommend("u");
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 40);
        assertFalse(f.isDone());
        f.cancel(true);
    }
}
