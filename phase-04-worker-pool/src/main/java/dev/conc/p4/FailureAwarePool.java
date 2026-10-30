package dev.conc.p4;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Challenge 4. A fixed pool that never loses a task failure. The constructor is given. Implement
 * afterExecute and failureCount so that:
 *  - every task that failed, whether it was started with execute(Runnable) or submit(...), is
 *    reported to onFailure exactly once with its ORIGINAL exception (not an ExecutionException);
 *  - a successful task reports nothing; a cancelled task is not a failure;
 *  - failureCount() counts the reported failures;
 *  - submit(...) still returns a Future whose get() throws ExecutionException as usual;
 *  - if onFailure itself throws, the pool keeps working.
 */
public final class FailureAwarePool extends ThreadPoolExecutor {
    private final Consumer<Throwable> onFailure;

    public FailureAwarePool(int threads, Consumer<Throwable> onFailure) {
        super(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.onFailure = onFailure;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        // TODO
    }

    public long failureCount() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
