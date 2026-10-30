package dev.conc.p5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Needs a machine with more than one core to see a loss. */
class C01RaceHarnessTest {

    @Test
    void oneThreadLosesNothing() {
        RaceResult r = RaceHarness.countWithoutSync(1, 100_000);
        assertEquals(100_000, r.expected());
        assertEquals(100_000, r.actual());
        assertEquals(0, r.lost());
    }

    @Test
    void expectedIsThreadsTimesIncrements() {
        RaceResult r = RaceHarness.countWithoutSync(4, 1000);
        assertEquals(4000, r.expected());
        assertTrue(r.actual() > 0 && r.actual() <= r.expected());
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void counterLosesUpdatesUnderContention() {
        long worstLoss = 0;
        for (int attempt = 0; attempt < 100 && worstLoss == 0; attempt++) {
            RaceResult r = RaceHarness.countWithoutSync(4, 1_000_000);
            assertTrue(r.actual() <= r.expected(), "a counter cannot count more than happened");
            worstLoss = Math.max(worstLoss, r.lost());
        }
        assertTrue(worstLoss > 0, "no lost update in 100 runs; is this a single-core machine?");
    }

    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void storeLosesEventsUnderContention() {
        List<Event> events = TestEvents.events(8_000, 64);
        long worstLoss = 0;
        for (int attempt = 0; attempt < 200 && worstLoss == 0; attempt++) {
            RaceResult r = RaceHarness.recordWithoutSync(8, events);
            assertEquals(events.size(), r.expected());
            assertTrue(r.actual() <= r.expected());
            worstLoss = Math.max(worstLoss, r.lost());
        }
        assertTrue(worstLoss > 0, "no lost event in 200 runs");
    }
}
