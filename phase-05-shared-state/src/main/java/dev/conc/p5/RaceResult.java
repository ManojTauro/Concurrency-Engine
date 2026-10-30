package dev.conc.p5;

/** What a run should have counted against what it counted. */
public record RaceResult(long expected, long actual) {
    public long lost() {
        return expected - actual;
    }
}
