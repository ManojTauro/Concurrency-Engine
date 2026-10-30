package dev.conc.p11;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

class C04KneeAndUslTest {

    private static final List<RatePoint> CURVE = List.of(
            new RatePoint(100, 10), new RatePoint(200, 11), new RatePoint(300, 13),
            new RatePoint(400, 35), new RatePoint(500, 400));

    @Test
    void theKneeIsTheFirstRateWhoseP99ExceedsTheFactorTimesTheBaseline() {
        assertEquals(OptionalDouble.of(400), Knee.find(CURVE, 3));
        assertEquals(OptionalDouble.of(500), Knee.find(CURVE, 3.5));
        assertEquals(OptionalDouble.of(300), Knee.find(CURVE, 1.2));
    }

    @Test
    void noKneeWhenLatencyStaysFlat() {
        assertEquals(OptionalDouble.empty(), Knee.find(CURVE, 100));
        assertEquals(OptionalDouble.empty(), Knee.find(List.of(), 2));
        assertEquals(OptionalDouble.empty(), Knee.find(List.of(new RatePoint(10, 5)), 2));
    }

    @Test
    void theComparisonIsStrict() {
        assertEquals(OptionalDouble.empty(), Knee.find(List.of(new RatePoint(1, 10), new RatePoint(2, 30)), 3));
        assertEquals(OptionalDouble.of(2), Knee.find(List.of(new RatePoint(1, 10), new RatePoint(2, 30.01)), 3));
    }

    @Test
    void badInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Knee.find(List.of(new RatePoint(2, 1), new RatePoint(1, 1)), 2));
        assertThrows(IllegalArgumentException.class, () -> Knee.find(List.of(new RatePoint(1, 1), new RatePoint(1, 1)), 2));
        assertThrows(IllegalArgumentException.class, () -> Knee.find(CURVE, 1));
    }

    @Test
    void amdahlLimitsTheSpeedup() {
        assertEquals(1.0, Scalability.amdahlSpeedup(0.9, 1), 1e-9);
        assertEquals(1 / (0.1 + 0.9 / 8), Scalability.amdahlSpeedup(0.9, 8), 1e-9);
        assertTrue(Scalability.amdahlSpeedup(0.9, 1_000_000) < 10.0001);
        assertEquals(4.0, Scalability.amdahlSpeedup(1.0, 4), 1e-9);
        assertEquals(1.0, Scalability.amdahlSpeedup(0.0, 64), 1e-9);
    }

    @Test
    void uslThroughputRisesThenFalls() {
        assertEquals(100.0, Scalability.uslThroughput(100, 0.05, 0.001, 1), 1e-9);
        assertEquals(800 / 1.406, Scalability.uslThroughput(100, 0.05, 0.001, 8), 1e-9);
        assertEquals(1600 / 1.99, Scalability.uslThroughput(100, 0.05, 0.001, 16), 1e-9);
        assertTrue(Scalability.uslThroughput(100, 0.05, 0.001, 100) < Scalability.uslThroughput(100, 0.05, 0.001, 31));
        assertEquals(100.0 * 7, Scalability.uslThroughput(100, 0, 0, 7), 1e-9, "no contention, no coherency: linear");
    }

    @Test
    void thePeakIsTheBetterOfTheTwoWholeNumbersAroundTheContinuousPeak() {
        // continuous peak sqrt(0.95 / 0.001) = 30.82, but 31 beats 30
        assertEquals(31, Scalability.uslPeakWorkers(0.05, 0.001));
        assertEquals(10, Scalability.uslPeakWorkers(0.0, 0.01));
        assertEquals(1, Scalability.uslPeakWorkers(1.0, 0.01));
        assertEquals(1, Scalability.uslPeakWorkers(1.5, 0.01));
        assertThrows(IllegalArgumentException.class, () -> Scalability.uslPeakWorkers(0.05, 0));
    }

    @Test
    void littlesLawGivesTheAverageNumberInTheSystem() {
        assertEquals(50.0, Scalability.littlesLawL(1_000, 0.05), 1e-9);
        assertEquals(20.0, Scalability.littlesLawL(500, 0.04), 1e-9);
        assertEquals(0.0, Scalability.littlesLawL(0, 3), 1e-9);
    }
}
