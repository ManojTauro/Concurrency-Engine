package dev.conc.p5;

/** Given. A counter that is wrong under concurrency. */
public final class UnsafeCounter {
    private long value;

    public void increment() {
        value++;
    }

    public long get() {
        return value;
    }
}
