package dev.conc.p9;

/** A monotonic clock, as an interface, so tests can move time by hand. */
@FunctionalInterface
public interface Clock {
    Clock SYSTEM = System::nanoTime;

    long nanos();
}
