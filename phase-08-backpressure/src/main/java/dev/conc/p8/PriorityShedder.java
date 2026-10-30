package dev.conc.p8;

import java.util.Map;

/**
 * Challenge 4. When overloaded, lose the least valuable work first. The priority of an event is
 * its EventType weight (VIEW 1 < CLICK 2 < ADD_TO_CART 5 < PURCHASE 10).
 *
 *  - offer(): if there is room, ACCEPTED. When full: find the queued event with the LOWEST weight
 *    (among equals, the one that arrived first); if the new event's weight is strictly higher, evict
 *    that one and return ACCEPTED_EVICTED; otherwise return REJECTED (the new event is lost);
 *  - poll(): the queued event with the HIGHEST weight, equal weights in arrival order; null if empty;
 *  - shedByType(): for every EventType, how many events of that type were lost (evicted or rejected);
 *  - the queue never holds more than capacity events, and all of this is safe from many threads;
 *  - capacity below 1 throws IllegalArgumentException.
 */
public final class PriorityShedder {

    public PriorityShedder(int capacity) {
        // TODO
    }

    public ShedOutcome offer(Event event) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Event poll() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Map<EventType, Long> shedByType() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
