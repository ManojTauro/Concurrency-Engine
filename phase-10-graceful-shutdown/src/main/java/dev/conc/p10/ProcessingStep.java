package dev.conc.p10;

/** One unit of work. Like all blocking work, it throws InterruptedException if its thread is interrupted. */
@FunctionalInterface
public interface ProcessingStep {
    void process(Event event) throws InterruptedException;
}
