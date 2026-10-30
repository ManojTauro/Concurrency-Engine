package dev.conc.p6;

import java.util.HashSet;
import java.util.Set;

/**
 * Given, complete, and NOT thread-safe on purpose. Several fields change together in apply(), so
 * whatever protects it has to cover the whole method.
 */
public final class UserBehaviourProfile {
    private final String userId;
    private final long[] counts = new long[EventType.values().length];
    private final Set<String> products = new HashSet<>();
    private long score;
    private long lastUserSeq;

    public UserBehaviourProfile(String userId) {
        this.userId = userId;
    }

    public String userId() {
        return userId;
    }

    public void apply(Event e) {
        counts[e.type().ordinal()]++;
        score += e.type().weight();
        products.add(e.productId());
        if (e.userSeq() > lastUserSeq) {
            lastUserSeq = e.userSeq();
        }
    }

    public long count(EventType type) {
        return counts[type.ordinal()];
    }

    public long score() {
        return score;
    }

    public int distinctProducts() {
        return products.size();
    }

    public long lastUserSeq() {
        return lastUserSeq;
    }
}
