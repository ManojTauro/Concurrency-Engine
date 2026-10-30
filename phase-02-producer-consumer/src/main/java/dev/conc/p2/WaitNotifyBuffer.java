package dev.conc.p2;

/**
 * Challenge 1. A bounded FIFO buffer built only from synchronized, wait and notify/notifyAll (no
 * java.util.concurrent). The constructor rejects a capacity below 1 with IllegalArgumentException.
 * Decide: how do you check the condition you wait for, and which of notify / notifyAll do you need?
 */
public final class WaitNotifyBuffer<T> implements Buffer<T> {

    public WaitNotifyBuffer(int capacity) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void put(T item) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public T take() throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int size() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
