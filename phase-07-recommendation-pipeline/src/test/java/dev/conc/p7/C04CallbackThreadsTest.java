package dev.conc.p7;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class C04CallbackThreadsTest {
    private ExecutorService cpu;

    private ExecutorService newCpuPool(int threads) {
        AtomicInteger n = new AtomicInteger();
        cpu = Executors.newFixedThreadPool(threads, r -> new Thread(r, "cpu-" + n.incrementAndGet()));
        return cpu;
    }

    @AfterEach
    void stop() {
        if (cpu != null) cpu.shutdownNow();
    }

    private static void burn(long millis) {
        long end = System.nanoTime() + millis * 1_000_000;
        while (System.nanoTime() < end) Thread.onSpinWait();
    }

    @Test
    void theHeavyStepRunsOnTheCpuPoolOnly() throws Exception {
        List<String> threads = Collections.synchronizedList(new ArrayList<>());
        RankStage stage = new RankStage(newCpuPool(2), list -> {
            threads.add(Thread.currentThread().getName());
            return list;
        });
        CompletableFuture<List<String>> in = Simulator.after(20, () -> List.of("a", "b"));
        assertEquals(List.of("a", "b"), stage.rank(in).get(10, TimeUnit.SECONDS));
        assertEquals(1, threads.size());
        assertTrue(threads.get(0).startsWith("cpu-"), "ran on " + threads.get(0));
    }

    @Test
    void alsoWhenTheInputIsAlreadyComplete() throws Exception {
        List<String> threads = Collections.synchronizedList(new ArrayList<>());
        RankStage stage = new RankStage(newCpuPool(2), list -> {
            threads.add(Thread.currentThread().getName());
            return list;
        });
        stage.rank(CompletableFuture.completedFuture(List.of("a"))).get(10, TimeUnit.SECONDS);
        assertTrue(threads.get(0).startsWith("cpu-"), "ran on " + threads.get(0));
    }

    @Test
    void heavyWorkDoesNotDelayOtherTimersOnTheSimulator() throws Exception {
        RankStage stage = new RankStage(newCpuPool(4), list -> {
            burn(25);
            return list;
        });
        List<CompletableFuture<List<String>>> outs = new ArrayList<>();
        for (int i = 0; i < 8; i++) outs.add(stage.rank(Simulator.after(10, () -> List.of("x"))));
        CompletableFuture<Long> timerLateness = new CompletableFuture<>();
        long due = System.nanoTime() + 30_000_000L;
        Simulator.schedule(30, () -> timerLateness.complete((System.nanoTime() - due) / 1_000_000));
        long late = timerLateness.get(10, TimeUnit.SECONDS);
        for (CompletableFuture<List<String>> f : outs) f.get(10, TimeUnit.SECONDS);
        assertTrue(late < 80, "a 30 ms timer fired " + late + " ms late: the simulator thread was busy with heavy work");
    }

    @Test
    void aFailingHeavyStepFailsTheResult() {
        RankStage stage = new RankStage(newCpuPool(1), list -> {
            throw new IllegalStateException("rank failed");
        });
        ExecutionException ex = assertThrows(ExecutionException.class,
                () -> stage.rank(CompletableFuture.completedFuture(List.of("a"))).get(10, TimeUnit.SECONDS));
        assertEquals("rank failed", ex.getCause().getMessage());
    }

    @Test
    void aFailingInputSkipsTheHeavyStepAndFailsTheResult() {
        AtomicInteger heavyCalls = new AtomicInteger();
        RankStage stage = new RankStage(newCpuPool(1), list -> {
            heavyCalls.incrementAndGet();
            return list;
        });
        assertThrows(ExecutionException.class,
                () -> stage.rank(CompletableFuture.failedFuture(new IllegalStateException("upstream"))).get(10, TimeUnit.SECONDS));
        assertEquals(0, heavyCalls.get());
    }
}
