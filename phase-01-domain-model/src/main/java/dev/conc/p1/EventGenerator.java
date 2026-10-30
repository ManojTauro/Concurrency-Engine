package dev.conc.p1;

import java.util.List;

/**
 * Deterministic source of events. Same seed, same stream. Later phases use it as the producer, so
 * it also decides how uneven the load is: with skew above zero a few users get most of the events.
 */
public final class EventGenerator {

    /**
     * @param seed     random seed
     * @param users    number of users, ids "user-0" .. "user-(users-1)"
     * @param products number of products, ids "product-0" .. "product-(products-1)"
     * @param skew     0 means every user is equally likely; above 0, user i has weight 1 / (i + 1)^skew
     */
    public EventGenerator(long seed, int users, int products, double skew) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Event next() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public List<Event> take(int n) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
