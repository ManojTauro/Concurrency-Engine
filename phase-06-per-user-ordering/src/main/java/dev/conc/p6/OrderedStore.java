package dev.conc.p6;

/** A profile store fed asynchronously. record() returns before the event is applied. */
public interface OrderedStore extends AutoCloseable {

    void record(Event event);

    /** Wait until every event recorded so far has been applied. */
    void awaitIdle() throws InterruptedException;

    long count(String userId, EventType type);

    long score(String userId);

    long totalEvents();

    /**
     * Number of applied events whose userSeq was not above the user's previous userSeq, that is,
     * events that arrived out of order (or twice) for their user.
     */
    long orderViolations();

    @Override
    void close();
}
