package dev.conc.p6;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Challenge 1. A fixed number of "lanes", each a single thread with its own queue. A task is sent
 * to the lane of its key, so tasks with the same key run one after another in submission order,
 * while tasks of keys in different lanes run in parallel.
 *
 *  - lane threads are named "p6-lane-0", "p6-lane-1", ...;
 *  - execute() after shutdown() throws RejectedExecutionException;
 *  - a task that throws must not stop its lane;
 *  - shutdown() lets queued tasks finish; awaitTermination() waits for the lane threads to end.
 */
public final class LaneExecutor {
    private final int lanes;

    public LaneExecutor(int lanes) {
        if (lanes < 1) throw new IllegalArgumentException("lanes");
        this.lanes = lanes;
        // TODO
    }

    /** Given: the lane of a key. Same key, same lane, always. */
    public int laneOf(String key) {
        return Math.floorMod(key.hashCode(), lanes);
    }

    public void execute(String key, Runnable task) throws RejectedExecutionException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void shutdown() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
