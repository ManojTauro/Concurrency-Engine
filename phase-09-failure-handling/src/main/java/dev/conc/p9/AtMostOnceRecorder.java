package dev.conc.p9;

import java.util.function.Consumer;

/**
 * Challenge 4, part 2. Make retries safe: applying an event twice must change nothing.
 *
 * record(event): if the event id has been seen, do nothing and return false. Otherwise pass the
 * event to the sink and return true. If the sink THROWS, the id must be forgotten again (so a
 * later retry of the same event can succeed) and the exception propagates.
 */
public final class AtMostOnceRecorder {

    public AtMostOnceRecorder(DedupFilter filter, Consumer<Event> sink) {
        // TODO
    }

    public boolean record(Event event) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
