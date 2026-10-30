package dev.conc.p2;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class C03PipelineTest {

    private static List<Event> events(int n) {
        List<Event> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Event(i + 1, "user-" + (i % 7), "product-" + (i % 13), EventType.values()[i % 4], i, i));
        }
        return list;
    }

    private static boolean p2ThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p2-"));
    }

    @Test
    void deliversEveryEventInSourceOrder() {
        List<Event> source = events(10_000);
        List<Event> sink = Collections.synchronizedList(new ArrayList<>());
        PipelineResult r = assertTimeoutPreemptively(Duration.ofSeconds(20), () -> EventPipeline.run(source, 16, sink::add));
        assertEquals(10_000, r.consumed());
        assertEquals(source, sink);
    }

    @Test
    void worksWithCapacityOne() {
        List<Event> source = events(2_000);
        List<Event> sink = Collections.synchronizedList(new ArrayList<>());
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> EventPipeline.run(source, 1, sink::add));
        assertEquals(source, sink);
    }

    @Test
    void emptySourceFinishes() {
        PipelineResult r = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> EventPipeline.run(List.of(), 4, e -> fail("no events expected")));
        assertEquals(0, r.consumed());
    }

    @Test
    void sinkRunsOnOneDedicatedConsumerThread() {
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> EventPipeline.run(events(500), 8, e -> names.add(Thread.currentThread().getName())));
        assertEquals(Set.of("p2-consumer"), names);
    }

    @Test
    void noPipelineThreadSurvivesASuccessfulRun() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> EventPipeline.run(events(500), 8, e -> {
        }));
        assertFalse(p2ThreadAlive());
    }

    @Test
    void aFailingSinkFailsTheRunWithoutHanging() {
        AtomicInteger calls = new AtomicInteger();
        PipelineFailedException ex = assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(PipelineFailedException.class, () -> EventPipeline.run(events(10_000), 4, e -> {
                    if (calls.incrementAndGet() == 10) throw new IllegalStateException("boom");
                })));
        assertInstanceOf(IllegalStateException.class, ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
        assertEquals(10, calls.get(), "the sink must not be called again after it failed");
        assertFalse(p2ThreadAlive(), "the producer must not be left blocked on a full queue");
    }
}
