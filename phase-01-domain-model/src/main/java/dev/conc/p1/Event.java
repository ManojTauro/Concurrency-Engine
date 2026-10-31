package dev.conc.p1;

import java.util.Objects;

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
        if (Objects.isNull(userId)) throw new NullPointerException("userId must not be null");
        if (Objects.isNull(productId)) throw new NullPointerException("productId must not be null");
        if (Objects.isNull(type)) throw new NullPointerException("type must not be null");

        if (userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (productId.isBlank()) throw new IllegalArgumentException("productId must not be blank");

        if (eventId <= 0) throw new IllegalArgumentException("eventId must be positive: >= 0");
        if (userSeq <= 0) throw new IllegalArgumentException("userSeq must be positive: >= 0");
        if (timestampMillis < 0)
            throw new IllegalArgumentException("timestamp must be greater than or equal to 0");
    }
}
