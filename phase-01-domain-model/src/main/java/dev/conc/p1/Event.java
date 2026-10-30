package dev.conc.p1;

/**
 * One user action. Immutable on purpose: later phases hand these between threads, and an
 * immutable object needs no synchronization to be shared safely.
 *
 * @param eventId         unique and increasing across the whole stream, starts at 1
 * @param userId          never null or blank
 * @param productId       never null or blank
 * @param type            never null
 * @param timestampMillis not negative
 * @param userSeq         this user's own event counter, starts at 1 (used in Phase 6 to check ordering)
 */
public record Event(long eventId, String userId, String productId, EventType type, long timestampMillis, long userSeq) {

    public Event {
        // TODO: reject invalid values (see the challenge README for the exact exception types)
    }
}
