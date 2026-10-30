package dev.conc.p2;

import java.util.concurrent.TimeUnit;

/**
 * Challenge 2. The same buffer built from one ReentrantLock and two Conditions, plus the timed
 * operations. The constructor rejects a capacity below 1 with IllegalArgumentException.
 */
public final class ConditionBuffer<T> implements TimedBuffer<T> {

    public ConditionBuffer(int capacity) {
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

    @Override
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
