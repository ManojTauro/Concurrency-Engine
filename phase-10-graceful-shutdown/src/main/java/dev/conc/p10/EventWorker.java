package dev.conc.p10;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Challenge 2. A worker that can be stopped by interrupting its thread, without losing an event or
 * hiding the interrupt. run() loops: take an event from the queue, run the step, count it.
 *
 *  - it stops when its thread is interrupted, whether that happens while it waits for an event or
 *    while the step is running (the step then throws InterruptedException);
 *  - an event whose step was interrupted is NOT counted as processed: interruptedEvent() returns it
 *    so that it can be handled again. An event taken and fully processed is counted in processed();
 *  - a RuntimeException from the step counts in failed() and the worker carries on;
 *  - when run() ends because of an interrupt the thread's interrupt flag is SET again (code that
 *    catches InterruptedException clears it; the owner of the thread must still be able to see it)
 *    and exitedWithInterruptFlag() is true;
 *  - nothing is lost: events processed + the interrupted one + the events left in the queue equal
 *    the events that were put in.
 */
public final class EventWorker implements Runnable {

    public EventWorker(BlockingQueue<Event> queue, ProcessingStep step) {
        // TODO
    }

    @Override
    public void run() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long processed() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long failed() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Event> interruptedEvent() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean exitedWithInterruptFlag() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
