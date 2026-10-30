package dev.conc.p4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class C02ExecutorConfigTest {
    private final CountDownLatch gate = new CountDownLatch(1);
    private ThreadPoolExecutor pool;

    @AfterEach
    void cleanup() {
        gate.countDown();
        if (pool != null) pool.shutdownNow();
    }

    private Runnable blocker() {
        return () -> {
            try {
                gate.await();
            } catch (InterruptedException ignored) {
            }
        };
    }

    private static PoolSpec spec(Rejection r, boolean prestart, long keepAlive) {
        return new PoolSpec("worker", 2, 4, 2, keepAlive, r, prestart);
    }

    @Test
    void threadsAreNamedWithThePrefixAndACounter() throws Exception {
        pool = PoolFactory.create(spec(Rejection.ABORT, false, 1000));
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch started = new CountDownLatch(2);
        for (int i = 0; i < 2; i++) pool.execute(() -> {
            names.add(Thread.currentThread().getName());
            started.countDown();
            blocker().run();
        });
        assertTrue(started.await(5, TimeUnit.SECONDS));
        assertEquals(Set.of("worker-1", "worker-2"), names);
    }

    @Test
    void threadsBeyondTheCoreOnlyAppearOnceTheQueueIsFull() {
        pool = PoolFactory.create(spec(Rejection.ABORT, false, 1000));
        pool.execute(blocker());
        pool.execute(blocker());
        assertEquals(2, pool.getPoolSize());
        pool.execute(blocker());
        pool.execute(blocker());
        assertEquals(2, pool.getPoolSize(), "tasks 3 and 4 wait in the queue");
        assertEquals(2, pool.getQueue().size());
        pool.execute(blocker());
        assertEquals(3, pool.getPoolSize(), "queue full, so the pool grows");
        pool.execute(blocker());
        assertEquals(4, pool.getPoolSize());
        assertEquals(2, pool.getQueue().size());
    }

    @Test
    void abortRejectsWhenQueueAndPoolAreFull() {
        pool = PoolFactory.create(spec(Rejection.ABORT, false, 1000));
        for (int i = 0; i < 6; i++) pool.execute(blocker());
        assertThrows(RejectedExecutionException.class, () -> pool.execute(blocker()));
    }

    @Test
    void callerRunsMakesTheSubmitterDoTheWork() {
        pool = PoolFactory.create(spec(Rejection.CALLER_RUNS, false, 1000));
        for (int i = 0; i < 6; i++) pool.execute(blocker());
        Thread[] ranOn = new Thread[1];
        pool.execute(() -> ranOn[0] = Thread.currentThread());
        assertSame(Thread.currentThread(), ranOn[0]);
    }

    @Test
    void discardOldestDropsTheLongestWaitingQueuedTask() throws Exception {
        pool = PoolFactory.create(spec(Rejection.DISCARD_OLDEST, false, 1000));
        Set<Integer> ran = Collections.synchronizedSet(new HashSet<>());
        for (int i = 1; i <= 6; i++) {
            int id = i;
            pool.execute(() -> {
                blocker().run();
                ran.add(id);
            });
        }
        pool.execute(() -> {
            blocker().run();
            ran.add(7);
        });
        gate.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(Set.of(1, 2, 4, 5, 6, 7), ran, "task 3 waited longest in the queue and is the one dropped");
    }

    @Test
    void prestartStartsTheCoreThreadsImmediately() {
        pool = PoolFactory.create(spec(Rejection.ABORT, true, 1000));
        assertEquals(2, pool.getPoolSize());
        ThreadPoolExecutor lazy = PoolFactory.create(spec(Rejection.ABORT, false, 1000));
        try {
            assertEquals(0, lazy.getPoolSize());
        } finally {
            lazy.shutdownNow();
        }
    }

    @Test
    void idleThreadsAboveTheCoreExpire() throws Exception {
        pool = PoolFactory.create(spec(Rejection.ABORT, false, 100));
        for (int i = 0; i < 6; i++) pool.execute(blocker());
        assertEquals(4, pool.getPoolSize());
        gate.countDown();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (pool.getPoolSize() > 2 && System.nanoTime() < deadline) Thread.sleep(20);
        assertEquals(2, pool.getPoolSize());
    }

    @Test
    void settingsAreAppliedToTheExecutor() {
        pool = PoolFactory.create(spec(Rejection.ABORT, false, 750));
        assertEquals(2, pool.getCorePoolSize());
        assertEquals(4, pool.getMaximumPoolSize());
        assertEquals(750, pool.getKeepAliveTime(TimeUnit.MILLISECONDS));
        assertEquals(2, pool.getQueue().remainingCapacity());
    }
}
