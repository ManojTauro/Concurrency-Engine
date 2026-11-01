package dev.conc.p1;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * What the engine knows about one user. Deliberately NOT thread-safe: Phase 5 shares it between
 * threads and breaks it on purpose.
 */
public final class UserBehaviourProfile {
    private final String userId;
    private long lastUserSeq;
    private long lastTimestampMillis = -1;
    private final Map<EventType, Long> typeCounts;
    private long score;
    private final Set<String> distinctProducts;

    public UserBehaviourProfile(String userId) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");

        if (userId.isBlank()) throw new IllegalArgumentException("userId cannot be blank");

        this.typeCounts = new EnumMap<>(EventType.class);
        this.distinctProducts = new HashSet<>();
    }

    public String userId() {
        return this.userId;
    }

    /** Fold one event into the profile. */
    public void apply(Event event) {
        Objects.requireNonNull(event, "event cannot be null");

        if (!Objects.equals(event.userId(), this.userId))
            throw new IllegalArgumentException("profile belongs to the user " + this.userId + ", but event was generated for " + event.userId());

        this.typeCounts.merge(event.type(), 1L, Long::sum);
        this.score += event.type().weight();
        this.distinctProducts.add(event.productId());

        this.lastUserSeq = Math.max(this.lastUserSeq, event.userSeq());
        this.lastTimestampMillis = Math.max(this.lastTimestampMillis, event.timestampMillis());
    }

    public long count(EventType type) {
        return this.typeCounts.getOrDefault(type, 0L);
    }

    /** Sum of the weights of all applied events. */
    public long score() {
        return this.score;
    }

    /** Number of different products seen. */
    public int distinctProducts() {
        return this.distinctProducts.size();
    }

    /** Highest userSeq applied so far, 0 if none. */
    public long lastUserSeq() {
        return this.lastUserSeq;
    }

    /** Largest timestamp applied so far, -1 if none. */
    public long lastTimestampMillis() {
        return this.lastTimestampMillis;
    }
}
