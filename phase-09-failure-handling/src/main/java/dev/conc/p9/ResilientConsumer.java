package dev.conc.p9;

import java.util.List;

/**
 * Challenge 2. One bad event must never stop the stream. For each event, in order:
 *  - call the handler; if it throws an Exception, call it again, up to maxAttempts calls in total
 *    (no waiting between calls here); the retries of an event happen before the next event starts;
 *  - when the handler succeeds, count it as succeeded;
 *  - when a PermanentFailureException is thrown, do not retry: add DeadLetter(event, thatException,
 *    attemptsSoFar) to the dead letter queue;
 *  - when all attempts failed, add DeadLetter(event, the LAST exception, maxAttempts);
 *  - an InterruptedException from the handler stops processAll at once and is rethrown; the event
 *    that was interrupted is NOT dead-lettered and later events are not touched.
 * maxAttempts below 1 throws IllegalArgumentException.
 */
public final class ResilientConsumer {

    public ResilientConsumer(EventHandler handler, int maxAttempts, DeadLetterQueue dlq) {
        // TODO
    }

    public Summary processAll(List<Event> events) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
