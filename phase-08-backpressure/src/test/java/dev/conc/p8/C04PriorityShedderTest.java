package dev.conc.p8;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class C04PriorityShedderTest {

    private static Event ev(long id, EventType type) {
        return new Event(id, "u", "p", type, 0, id);
    }

    @Test
    void capacityBelowOneIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PriorityShedder(0));
    }

    @Test
    void equalPriorityWhenFullIsRejected() {
        PriorityShedder s = new PriorityShedder(3);
        for (int i = 1; i <= 3; i++) assertEquals(ShedOutcome.ACCEPTED, s.offer(ev(i, EventType.VIEW)));
        assertEquals(ShedOutcome.REJECTED, s.offer(ev(4, EventType.VIEW)));
        assertEquals(1L, s.shedByType().get(EventType.VIEW));
        assertEquals(3, s.size());
    }

    @Test
    void aPurchaseEvictsTheOldestOfTheLowestPriority() {
        PriorityShedder s = new PriorityShedder(3);
        s.offer(ev(1, EventType.VIEW));
        s.offer(ev(2, EventType.VIEW));
        s.offer(ev(3, EventType.CLICK));
        assertEquals(ShedOutcome.ACCEPTED_EVICTED, s.offer(ev(4, EventType.PURCHASE)));
        assertEquals(1L, s.shedByType().get(EventType.VIEW));
        assertEquals(4, s.poll().eventId());
        assertEquals(3, s.poll().eventId());
        assertEquals(2, s.poll().eventId());
        assertNull(s.poll());
    }

    @Test
    void aLowerPriorityEventIsRejectedWhenFullOfBetterOnes() {
        PriorityShedder s = new PriorityShedder(2);
        s.offer(ev(1, EventType.PURCHASE));
        s.offer(ev(2, EventType.ADD_TO_CART));
        assertEquals(ShedOutcome.REJECTED, s.offer(ev(3, EventType.CLICK)));
        assertEquals(1L, s.shedByType().get(EventType.CLICK));
        assertEquals(0L, s.shedByType().get(EventType.PURCHASE));
    }

    @Test
    void pollGivesHighestPriorityFirstAndArrivalOrderWithinAPriority() {
        PriorityShedder s = new PriorityShedder(10);
        s.offer(ev(1, EventType.VIEW));
        s.offer(ev(2, EventType.PURCHASE));
        s.offer(ev(3, EventType.CLICK));
        s.offer(ev(4, EventType.PURCHASE));
        s.offer(ev(5, EventType.VIEW));
        List<Long> order = new ArrayList<>();
        for (Event e; (e = s.poll()) != null; ) order.add(e.eventId());
        assertEquals(List.of(2L, 4L, 3L, 1L, 5L), order);
    }

    @Test
    void shedCountsExistForEveryTypeAndStartAtZero() {
        PriorityShedder s = new PriorityShedder(1);
        Map<EventType, Long> m = s.shedByType();
        for (EventType t : EventType.values()) assertEquals(0L, m.get(t));
    }

    @Test
    void thePolicyHoldsStepByStepOnARandomStream() {
        PriorityShedder s = new PriorityShedder(20);
        Random rnd = new Random(7);
        Map<EventType, Integer> model = new EnumMap<>(EventType.class);
        for (EventType t : EventType.values()) model.put(t, 0);
        Map<EventType, Long> lastShed = new EnumMap<>(EventType.class);
        for (EventType t : EventType.values()) lastShed.put(t, 0L);
        int size = 0;
        for (int i = 1; i <= 5_000; i++) {
            EventType type = EventType.values()[rnd.nextInt(4)];
            ShedOutcome o = s.offer(ev(i, type));
            Map<EventType, Long> shed = s.shedByType();
            EventType lost = null;
            for (EventType t : EventType.values()) {
                if (shed.get(t) > lastShed.get(t)) {
                    assertNull(lost, "more than one event lost in one offer");
                    lost = t;
                }
                lastShed.put(t, shed.get(t));
            }
            EventType lowest = null;
            for (EventType t : EventType.values()) if (model.get(t) > 0 && lowest == null) lowest = t;
            switch (o) {
                case ACCEPTED -> {
                    assertNull(lost);
                    assertTrue(size < 20, "accepted into a full queue");
                    model.merge(type, 1, Integer::sum);
                    size++;
                }
                case REJECTED -> {
                    assertEquals(type, lost);
                    assertEquals(20, size);
                    assertTrue(type.weight() <= lowest.weight(), type + " was refused while " + lowest + " was queued");
                }
                case ACCEPTED_EVICTED -> {
                    assertEquals(lowest, lost, "must evict the lowest priority present");
                    assertTrue(type.weight() > lost.weight());
                    model.merge(lost, -1, Integer::sum);
                    model.merge(type, 1, Integer::sum);
                }
            }
            assertEquals(size, s.size());
        }
    }

    @Test
    void underContentionTheCapacityHoldsAndEveryEventIsAccountedFor() throws Exception {
        PriorityShedder s = new PriorityShedder(50);
        int producers = 8, each = 5_000;
        AtomicLong ids = new AtomicLong();
        List<Thread> ts = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            int seed = p;
            ts.add(new Thread(() -> {
                Random rnd = new Random(seed);
                for (int i = 0; i < each; i++) {
                    s.offer(ev(ids.incrementAndGet(), EventType.values()[rnd.nextInt(4)]));
                    if (s.size() > 50) throw new AssertionError("capacity exceeded: " + s.size());
                }
            }));
        }
        ts.forEach(Thread::start);
        for (Thread t : ts) t.join();
        long shed = s.shedByType().values().stream().mapToLong(Long::longValue).sum();
        assertEquals(producers * each, shed + s.size());
        assertEquals(50, s.size());
        int lowestKept = 99;
        for (Event e; (e = s.poll()) != null; ) lowestKept = Math.min(lowestKept, e.type().weight());
        assertTrue(lowestKept >= 5, "after 40,000 offers of every type, the queue should hold the valuable ones");
    }
}
