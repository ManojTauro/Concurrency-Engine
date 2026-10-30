package dev.conc.p9;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class C02DeadLetterTest {

    private static List<Event> events(int n) {
        List<Event> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) list.add(new Event(i, "u", "p", EventType.VIEW, 0, i));
        return list;
    }

    /** Fails event id N the first failuresBefore[N] times; ids in permanent fail with a permanent error. */
    private static final class Plan implements EventHandler {
        final Map<Long, Integer> transientFailures = new HashMap<>();
        final java.util.Set<Long> permanent = new java.util.HashSet<>();
        final List<String> calls = new ArrayList<>();
        final Map<Long, Integer> seen = new HashMap<>();

        @Override
        public void handle(Event e) throws Exception {
            int n = seen.merge(e.eventId(), 1, Integer::sum);
            calls.add(e.eventId() + "#" + n);
            if (permanent.contains(e.eventId())) throw new PermanentFailureException("bad " + e.eventId());
            if (n <= transientFailures.getOrDefault(e.eventId(), 0)) throw new IOException("flaky " + e.eventId() + " try " + n);
        }
    }

    @Test
    void healthyEventsAllSucceed() throws Exception {
        DeadLetterQueue dlq = new DeadLetterQueue();
        Summary s = new ResilientConsumer(new Plan(), 3, dlq).processAll(events(50));
        assertEquals(new Summary(50, 0), s);
        assertEquals(0, dlq.size());
    }

    @Test
    void transientFailuresAreRetriedBeforeTheNextEvent() throws Exception {
        Plan plan = new Plan();
        plan.transientFailures.put(2L, 2);
        DeadLetterQueue dlq = new DeadLetterQueue();
        Summary s = new ResilientConsumer(plan, 3, dlq).processAll(events(3));
        assertEquals(new Summary(3, 0), s);
        assertEquals(List.of("1#1", "2#1", "2#2", "2#3", "3#1"), plan.calls);
    }

    @Test
    void anEventThatKeepsFailingGoesToTheDeadLetterQueueAndTheStreamContinues() throws Exception {
        Plan plan = new Plan();
        plan.transientFailures.put(2L, 99);
        DeadLetterQueue dlq = new DeadLetterQueue();
        Summary s = new ResilientConsumer(plan, 3, dlq).processAll(events(4));
        assertEquals(new Summary(3, 1), s);
        assertEquals(1, dlq.size());
        DeadLetter dl = dlq.snapshot().get(0);
        assertEquals(2, dl.event().eventId());
        assertEquals(3, dl.attempts());
        assertEquals("flaky 2 try 3", dl.cause().getMessage(), "the cause is the LAST exception");
        assertTrue(plan.calls.contains("4#1"));
    }

    @Test
    void aPermanentFailureIsDeadLetteredWithoutRetry() throws Exception {
        Plan plan = new Plan();
        plan.permanent.add(3L);
        DeadLetterQueue dlq = new DeadLetterQueue();
        Summary s = new ResilientConsumer(plan, 5, dlq).processAll(events(5));
        assertEquals(new Summary(4, 1), s);
        DeadLetter dl = dlq.snapshot().get(0);
        assertEquals(1, dl.attempts());
        assertInstanceOf(PermanentFailureException.class, dl.cause());
        assertEquals(1, plan.seen.get(3L));
    }

    @Test
    void runtimeExceptionsAreRetriedLikeAnyOther() throws Exception {
        int[] calls = {0};
        DeadLetterQueue dlq = new DeadLetterQueue();
        Summary s = new ResilientConsumer(e -> {
            if (++calls[0] < 3) throw new IllegalStateException("npe-like");
        }, 3, dlq).processAll(events(1));
        assertEquals(new Summary(1, 0), s);
    }

    @Test
    void deadLettersKeepEventOrder() throws Exception {
        Plan plan = new Plan();
        plan.permanent.addAll(List.of(2L, 4L, 5L));
        DeadLetterQueue dlq = new DeadLetterQueue();
        new ResilientConsumer(plan, 2, dlq).processAll(events(6));
        assertEquals(List.of(2L, 4L, 5L), dlq.snapshot().stream().map(d -> d.event().eventId()).toList());
    }

    @Test
    void anInterruptionStopsTheRunWithoutDeadLetteringThatEvent() {
        DeadLetterQueue dlq = new DeadLetterQueue();
        List<Long> handled = new ArrayList<>();
        ResilientConsumer c = new ResilientConsumer(e -> {
            handled.add(e.eventId());
            if (e.eventId() == 3) throw new InterruptedException();
        }, 3, dlq);
        assertThrows(InterruptedException.class, () -> c.processAll(events(6)));
        assertEquals(List.of(1L, 2L, 3L), handled);
        assertEquals(0, dlq.size());
    }

    @Test
    void attemptsBelowOneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ResilientConsumer(e -> {
        }, 0, new DeadLetterQueue()));
    }
}
