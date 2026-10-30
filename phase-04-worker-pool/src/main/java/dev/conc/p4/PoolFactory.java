package dev.conc.p4;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Challenge 2. Build a ThreadPoolExecutor from a PoolSpec: a bounded ArrayBlockingQueue, named
 * threads, the chosen rejection policy, core threads started up front when spec.prestart() is true.
 * Idle threads above the core size must disappear after spec.keepAliveMillis().
 */
public final class PoolFactory {

    private PoolFactory() {
    }

    public static ThreadPoolExecutor create(PoolSpec spec) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
