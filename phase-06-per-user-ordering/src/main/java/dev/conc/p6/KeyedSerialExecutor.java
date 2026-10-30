package dev.conc.p6;

import java.util.concurrent.Executor;

/**
 * Challenge 2. Ordering per key WITHOUT a thread per key or per lane. Tasks with the same key run
 * one at a time, in submission order; tasks with different keys run in parallel on the shared pool.
 *
 *  - never more than one task of a key runs at once, and a task of a key never starts before the
 *    previous task of that key has finished;
 *  - memory for a key exists only while the key has work: activeKeys() is 0 when everything is done;
 *  - a task that throws must not stop later tasks of the same key;
 *  - fairness: one pool job runs at most {@code maxBatch} tasks of a key, then puts itself at the
 *    back of the pool's queue, so a key with a huge backlog cannot starve other keys;
 *  - maxBatch below 1 throws IllegalArgumentException.
 * The pool is given and is not owned by this class: do not shut it down, and note that it must stay
 * open until the keys are idle, because a job hands its key back to the pool.
 */
public final class KeyedSerialExecutor {

    public KeyedSerialExecutor(Executor pool, int maxBatch) {
        // TODO
    }

    public void execute(String key, Runnable task) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    /** Number of keys that currently have queued or running work. */
    public int activeKeys() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
