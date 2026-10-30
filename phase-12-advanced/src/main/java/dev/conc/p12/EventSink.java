package dev.conc.p12;

/** Where the server puts every event it receives. May throw. */
@FunctionalInterface
public interface EventSink {
    void accept(Event event) throws Exception;
}
