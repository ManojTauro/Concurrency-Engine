package dev.conc.p9;

/** The work done for one event. It may fail. */
@FunctionalInterface
public interface EventHandler {
    void handle(Event event) throws Exception;
}
