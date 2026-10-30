package dev.conc.p12;

/** Given. A cooperative CPU job: each slice burns CPU for min(quantum, remaining) and yields until nothing remains. */
public final class BurnSplit implements SplitRunner {
    private long remainingNanos;

    public BurnSplit(long totalNanos) {
        this.remainingNanos = totalNanos;
    }

    @Override
    public SplitResult process(long quantumNanos) {
        long slice = Math.min(quantumNanos, remainingNanos);
        long end = System.nanoTime() + slice;
        while (System.nanoTime() < end) Thread.onSpinWait();
        remainingNanos -= slice;
        return remainingNanos <= 0 ? new SplitResult.Finished() : new SplitResult.Yielded();
    }
}
