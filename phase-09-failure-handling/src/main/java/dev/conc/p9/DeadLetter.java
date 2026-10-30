package dev.conc.p9;

/** An event that could not be handled, why, and how many attempts were made. */
public record DeadLetter(Event event, Throwable cause, int attempts) {
}
