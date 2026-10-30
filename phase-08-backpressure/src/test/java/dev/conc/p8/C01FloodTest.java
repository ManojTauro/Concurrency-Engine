package dev.conc.p8;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class C01FloodTest {

    private static boolean p8ThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p8-"));
    }

    @Test
    void aFasterProducerMakesTheQueueGrowWithoutLimit() {
        FloodResult r = FloodExperiment.run(Duration.ofMillis(600), 20_000, 1_000);
        List<Integer> d = r.depthSamples();
        assertTrue(d.size() >= 8 && d.size() <= 14, "samples: " + d.size());
        assertTrue(d.get(d.size() - 1) > d.get(0), "depth should rise: " + d);
        assertTrue(d.get(d.size() - 1) > 5_000, "final depth " + d.get(d.size() - 1));
        assertTrue(r.produced() > r.consumed() * 5, "produced " + r.produced() + ", consumed " + r.consumed());
    }

    @Test
    void productionAndConsumptionFollowTheirRates() {
        FloodResult r = FloodExperiment.run(Duration.ofMillis(600), 10_000, 1_000);
        assertTrue(r.produced() > 3_500 && r.produced() < 8_000, "produced " + r.produced());
        assertTrue(r.consumed() > 300 && r.consumed() < 900, "consumed " + r.consumed());
    }

    @Test
    void aConsumerThatKeepsUpLeavesTheQueueShort() {
        FloodResult r = FloodExperiment.run(Duration.ofMillis(600), 2_000, 20_000);
        int max = r.depthSamples().stream().mapToInt(Integer::intValue).max().orElse(0);
        assertTrue(max < 500, "max depth " + max);
        assertTrue(r.consumed() > r.produced() * 0.8, "consumed " + r.consumed() + " of " + r.produced());
    }

    @Test
    void noThreadIsLeftRunning() {
        FloodExperiment.run(Duration.ofMillis(200), 1_000, 500);
        assertFalse(p8ThreadAlive());
    }
}
