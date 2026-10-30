package dev.conc.p9;

import java.time.Duration;

/** Waiting, as an interface, so tests can run a ten-minute backoff in a millisecond. */
@FunctionalInterface
public interface Sleeper {
    Sleeper REAL = d -> Thread.sleep(d.toMillis(), (int) (d.toNanos() % 1_000_000));

    void sleep(Duration duration) throws InterruptedException;
}
