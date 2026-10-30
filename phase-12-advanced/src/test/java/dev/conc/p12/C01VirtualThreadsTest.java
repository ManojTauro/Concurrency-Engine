package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class C01VirtualThreadsTest {

    @Test
    void tenThousandBlockingTasksFinishInAboutOneWaitNotTenThousand() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            int n = i;
            tasks.add(() -> {
                Thread.sleep(200);
                return n;
            });
        }
        long t0 = System.nanoTime();
        List<Outcome<Integer>> out = assertTimeoutPreemptively(Duration.ofSeconds(30), () -> FanOut.runAll(tasks));
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertEquals(10_000, out.size());
        for (int i = 0; i < 10_000; i++) assertEquals(i, out.get(i).value());
        assertTrue(ms < 5_000, "took " + ms + " ms; 10,000 waits of 200 ms on a pool of 200 threads would take 10 s");
    }

    @Test
    void everyTaskRunsOnItsOwnVirtualThread() {
        List<Callable<Boolean>> tasks = new ArrayList<>();
        List<Callable<Thread>> threads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tasks.add(() -> Thread.currentThread().isVirtual());
            threads.add(Thread::currentThread);
        }
        FanOut.runAll(tasks).forEach(o -> assertTrue(o.value(), "task did not run on a virtual thread"));
        List<Outcome<Thread>> ts = FanOut.runAll(threads);
        assertEquals(50, ts.stream().map(Outcome::value).distinct().count(), "one thread per task");
    }

    @Test
    void aFailingTaskGivesItsOriginalExceptionAndDoesNotAffectTheOthers() {
        IllegalStateException boom = new IllegalStateException("boom");
        List<Callable<String>> tasks = List.of(() -> "a", () -> {
            throw boom;
        }, () -> "c");
        List<Outcome<String>> out = FanOut.runAll(tasks);
        assertEquals("a", out.get(0).value());
        assertNull(out.get(0).error());
        assertNull(out.get(1).value());
        assertSame(boom, out.get(1).error());
        assertEquals("c", out.get(2).value());
    }

    @Test
    void noTasksGiveNoOutcomes() {
        assertEquals(List.of(), FanOut.runAll(List.<Callable<String>>of()));
    }

    @Test
    void theGivenPlatformVersionShowsWhyVirtualThreadsHelp() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 400; i++) tasks.add(() -> {
            Thread.sleep(100);
            return 1;
        });
        long t0 = System.nanoTime();
        PlatformFanOut.runAll(tasks, 20);
        long platform = (System.nanoTime() - t0) / 1_000_000;
        t0 = System.nanoTime();
        FanOut.runAll(tasks);
        long virtual = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(virtual * 5 < platform, "virtual " + virtual + " ms, 20 platform threads " + platform + " ms");
    }
}
