package dev.conc.p12;

/** Given. An item together with the scheduled time it has used so far. */
public final class LevelEntry<T> {
    private final T item;
    private final long[] thresholds;
    private long scheduledNanos;

    LevelEntry(T item, long[] thresholds) {
        this.item = item;
        this.thresholds = thresholds;
    }

    public T item() {
        return item;
    }

    public long scheduledNanos() {
        return scheduledNanos;
    }

    /** The level for the time used so far: the highest i with scheduledNanos >= thresholds[i]. */
    public int level() {
        int level = 0;
        for (int i = 0; i < thresholds.length; i++) if (scheduledNanos >= thresholds[i]) level = i;
        return level;
    }

    void addScheduled(long nanos) {
        scheduledNanos += nanos;
    }
}
