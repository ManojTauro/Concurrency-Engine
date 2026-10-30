package dev.conc.p9;

/**
 * Challenge 4, part 1. Remember which event ids were already seen, in bounded memory.
 *
 *  - firstTime(id): true if the id is not remembered (and it becomes remembered), false if it is.
 *    The check and the remembering are ONE atomic step: when many threads call it with the same
 *    id at the same moment, exactly one gets true;
 *  - at most {@code capacity} ids are remembered; when a new id would exceed that, the id that
 *    was remembered FIRST is forgotten (a repeat hit does not refresh an id);
 *  - forget(id) removes an id so that firstTime(id) is true again;
 *  - size() is the number of ids remembered;
 *  - capacity below 1 throws IllegalArgumentException.
 */
public final class DedupFilter {

    public DedupFilter(int capacity) {
        // TODO
    }

    public boolean firstTime(long eventId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void forget(long eventId) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
