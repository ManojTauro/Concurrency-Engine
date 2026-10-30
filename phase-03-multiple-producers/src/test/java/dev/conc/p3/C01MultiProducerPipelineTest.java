package dev.conc.p3;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class C01MultiProducerPipelineTest {

    /** Source s has events with userId "src-s" and userSeq 1..n, so order inside a source can be checked. */
    private static List<List<Event>> sources(int count, int each) {
        List<List<Event>> all = new ArrayList<>();
        long id = 1;
        for (int s = 0; s < count; s++) {
            List<Event> list = new ArrayList<>();
            for (int i = 1; i <= each; i++) list.add(new Event(id++, "src-" + s, "p" + i, EventType.VIEW, i, i));
            all.add(list);
        }
        return all;
    }

    private static boolean p3ThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p3-"));
    }

    @Test
    void everyEventArrivesAndEachSourceKeepsItsOrder() {
        List<List<Event>> src = sources(8, 5_000);
        List<Event> sink = Collections.synchronizedList(new ArrayList<>());
        long n = assertTimeoutPreemptively(Duration.ofSeconds(30), () -> MultiProducerPipeline.run(src, 4, sink::add));
        assertEquals(40_000, n);
        assertEquals(40_000, sink.size());
        for (int s = 0; s < 8; s++) {
            String user = "src-" + s;
            long last = 0;
            for (Event e : sink) {
                if (!e.userId().equals(user)) continue;
                assertEquals(last + 1, e.userSeq(), user + " out of order");
                last = e.userSeq();
            }
            assertEquals(5_000, last);
        }
    }

    @Test
    void capacityOneAndManyProducersDoesNotDeadlock() {
        List<List<Event>> src = sources(16, 500);
        List<Event> sink = Collections.synchronizedList(new ArrayList<>());
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> MultiProducerPipeline.run(src, 1, sink::add));
        assertEquals(8_000, sink.size());
    }

    @Test
    void sinkRunsOnTheConsumerThreadOnly() {
        Set<String> names = Collections.synchronizedSet(new HashSet<>());
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> MultiProducerPipeline.run(sources(4, 200), 8, e -> names.add(Thread.currentThread().getName())));
        assertEquals(Set.of("p3-consumer"), names);
    }

    @Test
    void producersOfVeryDifferentLengthAllFinish() {
        List<List<Event>> src = new ArrayList<>(sources(3, 10));
        src.addAll(sources(1, 20_000));
        List<Event> sink = Collections.synchronizedList(new ArrayList<>());
        long n = assertTimeoutPreemptively(Duration.ofSeconds(30), () -> MultiProducerPipeline.run(src, 8, sink::add));
        assertEquals(20_030, n);
    }

    @Test
    void noSourcesOrOnlyEmptySourcesReturnZero() {
        assertEquals(0, assertTimeoutPreemptively(Duration.ofSeconds(5), () -> MultiProducerPipeline.run(List.of(), 4, e -> fail())));
        assertEquals(0, assertTimeoutPreemptively(Duration.ofSeconds(5), () -> MultiProducerPipeline.run(List.of(List.of(), List.of()), 4, e -> fail())));
    }

    @Test
    void noPipelineThreadSurvives() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> MultiProducerPipeline.run(sources(5, 300), 4, e -> {
        }));
        assertFalse(p3ThreadAlive());
    }
}
