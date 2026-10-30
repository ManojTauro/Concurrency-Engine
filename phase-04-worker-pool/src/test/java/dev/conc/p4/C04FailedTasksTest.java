package dev.conc.p4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C04FailedTasksTest {

    @Test
    void aFailureFromExecuteIsReportedWithItsOriginalException() throws Exception {
        List<Throwable> seen = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch reported = new CountDownLatch(1);
        FailureAwarePool pool = new FailureAwarePool(2, t -> {
            seen.add(t);
            reported.countDown();
        });
        IllegalStateException boom = new IllegalStateException("x");
        pool.execute(() -> {
            throw boom;
        });
        assertTrue(reported.await(5, TimeUnit.SECONDS));
        assertSame(boom, seen.get(0));
        pool.shutdown();
    }

    @Test
    void aFailureFromSubmitIsReportedAndStillVisibleThroughTheFuture() throws Exception {
        List<Throwable> seen = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch reported = new CountDownLatch(1);
        FailureAwarePool pool = new FailureAwarePool(2, t -> {
            seen.add(t);
            reported.countDown();
        });
        IOException io = new IOException("disk");
        Future<Object> f = pool.submit(() -> {
            throw io;
        });
        assertTrue(reported.await(5, TimeUnit.SECONDS));
        assertSame(io, seen.get(0), "the handler must get the IOException, not an ExecutionException");
        ExecutionException ee = assertThrows(ExecutionException.class, f::get);
        assertSame(io, ee.getCause());
        pool.shutdown();
    }

    @Test
    void successfulAndCancelledTasksAreNotFailures() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FailureAwarePool pool = new FailureAwarePool(2, t -> calls.incrementAndGet());
        for (int i = 0; i < 20; i++) pool.submit(() -> 1);
        Future<?> slow = pool.submit(() -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException ignored) {
            }
        });
        Thread.sleep(100);
        slow.cancel(true);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, calls.get());
        assertEquals(0, pool.failureCount());
    }

    @Test
    void everyFailureIsCountedExactlyOnceUnderConcurrentSubmission() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FailureAwarePool pool = new FailureAwarePool(4, t -> calls.incrementAndGet());
        List<Thread> submitters = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            submitters.add(new Thread(() -> {
                for (int i = 0; i < 200; i++) {
                    boolean fail = i % 4 == 0;
                    if (i % 2 == 0) {
                        pool.execute(() -> {
                            if (fail) throw new IllegalStateException();
                        });
                    } else {
                        pool.submit(() -> {
                            if (fail) throw new IllegalStateException();
                        });
                    }
                }
            }));
        }
        submitters.forEach(Thread::start);
        for (Thread t : submitters) t.join();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(150, calls.get());
        assertEquals(150, pool.failureCount());
    }

    @Test
    void aHandlerThatThrowsDoesNotStopThePool() throws Exception {
        FailureAwarePool pool = new FailureAwarePool(2, t -> {
            throw new RuntimeException("handler broke");
        });
        for (int i = 0; i < 10; i++) pool.execute(() -> {
            throw new IllegalStateException();
        });
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < 50; i++) pool.execute(ran::incrementAndGet);
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(50, ran.get());
    }
}
