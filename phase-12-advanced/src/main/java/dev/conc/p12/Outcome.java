package dev.conc.p12;

/** Result of one task: either a value or the exception it threw. */
public record Outcome<T>(T value, Throwable error) {
}
