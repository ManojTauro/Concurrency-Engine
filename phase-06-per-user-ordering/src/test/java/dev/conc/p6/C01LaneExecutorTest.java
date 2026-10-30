package dev.conc.p6;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class C01LaneExecutorTest {

    @Test
    void tasksOfOneKeyRunInSubmissionOrder() throws Exception {
        LaneExecutor ex = new LaneExecutor(4);
        Map<String, List<Integer>> seen = new ConcurrentHashMap<>();
        for (int i = 0; i < 1_000; i++) {
            for (int k = 0; k < 10; k++) {
                String key = "key-" + k;
                int n = i;
                ex.execute(key, () -> seen.computeIfAbsent(key, x -> Collections.synchronizedList(new ArrayList<>())).add(n));
            }
        }
        ex.shutdown();
        assertTrue(ex.awaitTermination(10, TimeUnit.SECONDS));
        for (int k = 0; k < 10; k++) {
            List<Integer> list = seen.get("key-" + k);
            assertEquals(1_000, list.size());
            for (int i = 0; i < 1_000; i++) assertEquals(i, list.get(i), "key-" + k);
        }
    }

    @Test
    void keysInDifferentLanesRunInParallel() throws Exception {
        LaneExecutor ex = new LaneExecutor(4);
        String a = "a", b = null;
        for (int i = 0; i < 100 && b == null; i++) if (ex.laneOf("b" + i) != ex.laneOf(a)) b = "b" + i;
        assertNotNull(b);
        CountDownLatch aStarted = new CountDownLatch(1), bStarted = new CountDownLatch(1);
        CountDownLatch bothMet = new CountDownLatch(2);
        ex.execute(a, () -> {
            aStarted.countDown();
            try {
                if (bStarted.await(5, TimeUnit.SECONDS)) bothMet.countDown();
            } catch (InterruptedException ignored) {
            }
        });
        ex.execute(b, () -> {
            bStarted.countDown();
            try {
                if (aStarted.await(5, TimeUnit.SECONDS)) bothMet.countDown();
            } catch (InterruptedException ignored) {
            }
        });
        assertTrue(bothMet.await(10, TimeUnit.SECONDS), "the two tasks never ran at the same time");
        ex.shutdown();
        assertTrue(ex.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void aKeyAlwaysRunsOnTheSameNamedThread() throws Exception {
        LaneExecutor ex = new LaneExecutor(3);
        Set<String> threadsOfKey = Collections.synchronizedSet(new HashSet<>());
        Set<String> all = Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < 200; i++) {
            ex.execute("user-7", () -> threadsOfKey.add(Thread.currentThread().getName()));
            ex.execute("user-" + i, () -> all.add(Thread.currentThread().getName()));
        }
        ex.shutdown();
        assertTrue(ex.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, threadsOfKey.size());
        assertEquals("p6-lane-" + ex.laneOf("user-7"), threadsOfKey.iterator().next());
        assertTrue(all.size() <= 3);
        all.forEach(n -> assertTrue(n.startsWith("p6-lane-"), n));
    }

    @Test
    void aThrowingTaskDoesNotStopItsLane() throws Exception {
        LaneExecutor ex = new LaneExecutor(2);
        ex.execute("k", () -> {
            throw new IllegalStateException("boom");
        });
        List<Integer> ran = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            int n = i;
            ex.execute("k", () -> ran.add(n));
        }
        ex.shutdown();
        assertTrue(ex.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(20, ran.size());
    }

    @Test
    void rejectsAfterShutdownButFinishesQueuedWork() throws Exception {
        LaneExecutor ex = new LaneExecutor(1);
        CountDownLatch gate = new CountDownLatch(1);
        List<Integer> ran = Collections.synchronizedList(new ArrayList<>());
        ex.execute("k", () -> {
            try {
                gate.await();
            } catch (InterruptedException ignored) {
            }
        });
        for (int i = 0; i < 5; i++) {
            int n = i;
            ex.execute("k", () -> ran.add(n));
        }
        ex.shutdown();
        assertThrows(RejectedExecutionException.class, () -> ex.execute("k", () -> {
        }));
        gate.countDown();
        assertTrue(ex.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(5, ran.size());
    }

    @Test
    void lanesBelowOneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LaneExecutor(0));
    }
}
