package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class C04LimiterAndScopedValueTest {

    @Test
    void neverMoreThanThePermitsRunAtOnce() throws Exception {
        ConcurrencyLimiter lim = new ConcurrencyLimiter(3);
        AtomicInteger now = new AtomicInteger(), max = new AtomicInteger();
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            ts.add(new Thread(() -> {
                try {
                    for (int k = 0; k < 20; k++) {
                        lim.call(() -> {
                            max.accumulateAndGet(now.incrementAndGet(), Math::max);
                            Thread.sleep(1);
                            now.decrementAndGet();
                            return null;
                        });
                    }
                } catch (Exception ignored) {
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(3, max.get());
        assertEquals(0, lim.inUse());
    }

    @Test
    void aFailingActionGivesItsPermitBackAndItsExceptionPropagates() throws Exception {
        ConcurrencyLimiter lim = new ConcurrencyLimiter(1);
        IllegalStateException boom = new IllegalStateException("boom");
        for (int i = 0; i < 5; i++) {
            Exception e = assertThrows(Exception.class, () -> lim.call(() -> {
                throw boom;
            }));
            assertSame(boom, e);
        }
        assertEquals("ok", lim.call(() -> "ok"));
        assertEquals(0, lim.inUse());
    }

    @Test
    void tryCallGivesUpAfterTheTimeoutAndRunsWhenAPermitIsFree() throws Exception {
        ConcurrencyLimiter lim = new ConcurrencyLimiter(1);
        CountDownLatch inside = new CountDownLatch(1), release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            try {
                lim.call(() -> {
                    inside.countDown();
                    release.await();
                    return null;
                });
            } catch (Exception ignored) {
            }
        });
        holder.start();
        assertTrue(inside.await(5, TimeUnit.SECONDS));
        assertEquals(1, lim.inUse());
        long t0 = System.nanoTime();
        assertEquals(Optional.empty(), lim.tryCall(() -> "never", Duration.ofMillis(100)));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms >= 90 && ms < 1_500, "waited " + ms + " ms");
        release.countDown();
        holder.join();
        assertEquals(Optional.of("now"), lim.tryCall(() -> "now", Duration.ofSeconds(1)));
    }

    @Test
    void permitsBelowOneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ConcurrencyLimiter(0));
    }

    @Test
    void theRequestIdIsBoundInsideAndGoneOutside() throws Exception {
        assertEquals(Optional.empty(), RequestContext.currentRequestId());
        String seen = RequestContext.withRequestId("req-1", () -> RequestContext.currentRequestId().orElse("none"));
        assertEquals("req-1", seen);
        assertEquals(Optional.empty(), RequestContext.currentRequestId());
    }

    @Test
    void bindingsNestAndTheOuterOneComesBack() throws Exception {
        List<String> trace = new ArrayList<>();
        RequestContext.withRequestId("outer", () -> {
            trace.add(RequestContext.currentRequestId().orElseThrow());
            RequestContext.withRequestId("inner", () -> {
                trace.add(RequestContext.currentRequestId().orElseThrow());
                return null;
            });
            trace.add(RequestContext.currentRequestId().orElseThrow());
            return null;
        });
        assertEquals(List.of("outer", "inner", "outer"), trace);
    }

    @Test
    void anExceptionPropagatesAndTheBindingIsStillRemoved() {
        IllegalStateException boom = new IllegalStateException("boom");
        Exception e = assertThrows(Exception.class, () -> RequestContext.withRequestId("x", () -> {
            throw boom;
        }));
        assertSame(boom, e);
        assertEquals(Optional.empty(), RequestContext.currentRequestId());
    }

    @Test
    void aNewThreadStartedInsideDoesNotInheritTheBinding() throws Exception {
        AtomicReference<Optional<String>> inChild = new AtomicReference<>();
        RequestContext.withRequestId("parent", () -> {
            Thread t = new Thread(() -> inChild.set(RequestContext.currentRequestId()));
            t.start();
            t.join();
            return null;
        });
        assertEquals(Optional.empty(), inChild.get());
    }

    @Test
    void eachThreadHasItsOwnBinding() throws Exception {
        List<Thread> ts = new ArrayList<>();
        List<String> failures = java.util.Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 16; i++) {
            String id = "req-" + i;
            ts.add(new Thread(() -> {
                try {
                    RequestContext.withRequestId(id, () -> {
                        Thread.sleep(20);
                        if (!RequestContext.currentRequestId().orElse("").equals(id)) failures.add(id);
                        return null;
                    });
                } catch (Exception e) {
                    failures.add(e.toString());
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        assertEquals(List.of(), failures);
    }
}
