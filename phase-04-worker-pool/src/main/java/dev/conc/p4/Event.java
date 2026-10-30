package dev.conc.p4;

/** Immutable, so it can be shared between threads freely. A complete copy of Phase 1's Event. */
public record Event(long eventId, String userId, String productId, EventType type, long timestampMillis, long userSeq) {
}
