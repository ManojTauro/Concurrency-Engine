package dev.conc.p7;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.UnaryOperator;

/**
 * Challenge 4. A pipeline stage that does heavy CPU work on the result of a remote call. The
 * future you receive completes on the simulator thread ("p7-sim"), the one thread that also fires
 * every timer. Attach the heavy step so that it runs on the given cpuPool, never on "p7-sim" and
 * never on the common pool, and so that a heavy step that throws fails the returned future.
 */
public final class RankStage {

    public RankStage(Executor cpuPool, UnaryOperator<List<String>> heavy) {
        // TODO
    }

    public CompletableFuture<List<String>> rank(CompletableFuture<List<String>> candidates) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
