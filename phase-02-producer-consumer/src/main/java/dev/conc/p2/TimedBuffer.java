package dev.conc.p2;

import java.util.concurrent.TimeUnit;

/** A Buffer whose waits can be given up after a deadline. */
public interface TimedBuffer<T> extends Buffer<T> {

    /** Add an item, waiting up to the timeout for space. Returns false if no space appeared in time. */
    boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException;

    /** Remove the oldest item, waiting up to the timeout for one to arrive. Returns null if none arrived in time. */
    T poll(long timeout, TimeUnit unit) throws InterruptedException;
}
