package dev.conc.p5;

import java.util.HashMap;
import java.util.Map;

/** Given. The broken baseline: plain HashMap, plain counter, no protection at all. */
public final class UnsafeProfileStore implements ProfileStore {
    private final Map<String, UserBehaviourProfile> profiles = new HashMap<>();
    private long total;

    @Override
    public void record(Event event) {
        profiles.computeIfAbsent(event.userId(), UserBehaviourProfile::new).apply(event);
        total++;
    }

    @Override
    public long count(String userId, EventType type) {
        UserBehaviourProfile p = profiles.get(userId);
        return p == null ? 0 : p.count(type);
    }

    @Override
    public long score(String userId) {
        UserBehaviourProfile p = profiles.get(userId);
        return p == null ? 0 : p.score();
    }

    @Override
    public long totalEvents() {
        return total;
    }
}
