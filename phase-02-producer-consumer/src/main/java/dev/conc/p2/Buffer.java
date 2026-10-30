package dev.conc.p2;

/** A bounded FIFO buffer shared by producer and consumer threads. */
public interface Buffer<T> {

    /** Add an item, waiting while the buffer is full. Null items are rejected with NullPointerException. */
    void put(T item) throws InterruptedException;

    /** Remove the oldest item, waiting while the buffer is empty. */
    T take() throws InterruptedException;

    /** Number of items currently inside. */
    int size();
}
