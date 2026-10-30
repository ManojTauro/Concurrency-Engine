package dev.conc.p10;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C02InterruptibleWorkerTest {

    private static void await(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) fail("condition not reached within 5 s");
            Thread.sleep(5);
        }
    }

    private static Event ev(long id) {
        return new Event(id, "u", "p", EventType.VIEW, 0, id);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void aWorkerWaitingForEventsStopsPromptlyWhenInterrupted() throws Exception {
        BlockingQueue<Event> q = new ArrayBlockingQueue<>(10);
        EventWorker w = new EventWorker(q, e -> {
        });
        Thread t = new Thread(w);
        t.start();
        Thread.sleep(100);
        long t0 = System.nanoTime();
        t.interrupt();
        t.join(2_000);
        assertFalse(t.isAlive());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 500);
        assertTrue(w.interruptedEvent().isEmpty());
        assertTrue(w.exitedWithInterruptFlag());
        assertEquals(0, w.processed());
    }

    @Test
    void processesEventsInOrderUntilInterrupted() throws Exception {
        BlockingQueue<Event> q = new ArrayBlockingQueue<>(100);
        java.util.List<Long> seen = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        EventWorker w = new EventWorker(q, e -> seen.add(e.eventId()));
        for (int i = 1; i <= 50; i++) q.put(ev(i));
        Thread t = new Thread(w);
        t.start();
        await(() -> w.processed() >= 50);
        t.interrupt();
        t.join(2_000);
        assertEquals(50, w.processed());
        for (int i = 0; i < 50; i++) assertEquals(i + 1L, seen.get(i));
    }

    @Test
    void anEventInterruptedMidStepIsReturnedNotCounted() throws Exception {
        BlockingQueue<Event> q = new ArrayBlockingQueue<>(10);
        AtomicInteger inStep = new AtomicInteger();
        EventWorker w = new EventWorker(q, e -> {
            inStep.set((int) e.eventId());
            Thread.sleep(10_000);
        });
        q.put(ev(1));
        q.put(ev(2));
        Thread t = new Thread(w);
        t.start();
        await(() -> inStep.get() != 0);
        t.interrupt();
        t.join(2_000);
        assertFalse(t.isAlive());
        assertEquals(1, w.interruptedEvent().orElseThrow().eventId());
        assertEquals(0, w.processed());
        assertEquals(1, q.size(), "event 2 was never taken");
        assertTrue(w.exitedWithInterruptFlag());
    }

    @Test
    void nothingIsLostOrDuplicatedWhateverTheMomentOfTheInterrupt() throws Exception {
        for (int round = 0; round < 40; round++) {
            BlockingQueue<Event> q = new ArrayBlockingQueue<>(100);
            EventWorker w = new EventWorker(q, e -> sleep(1));
            for (int i = 1; i <= 100; i++) q.put(ev(i));
            Thread t = new Thread(w);
            t.start();
            Thread.sleep(5 + round);
            t.interrupt();
            t.join(2_000);
            assertFalse(t.isAlive(), "round " + round);
            long total = w.processed() + w.failed() + (w.interruptedEvent().isPresent() ? 1 : 0) + q.size();
            assertEquals(100, total, "round " + round);
            assertTrue(w.exitedWithInterruptFlag(), "round " + round);
        }
    }

    @Test
    void aRuntimeExceptionInTheStepDoesNotStopTheWorker() throws Exception {
        BlockingQueue<Event> q = new ArrayBlockingQueue<>(100);
        EventWorker w = new EventWorker(q, e -> {
            if (e.eventId() % 3 == 0) throw new IllegalStateException("bad " + e.eventId());
        });
        for (int i = 1; i <= 30; i++) q.put(ev(i));
        Thread t = new Thread(w);
        t.start();
        await(() -> w.processed() + w.failed() >= 30);
        t.interrupt();
        t.join(2_000);
        assertEquals(20, w.processed());
        assertEquals(10, w.failed());
    }
}
