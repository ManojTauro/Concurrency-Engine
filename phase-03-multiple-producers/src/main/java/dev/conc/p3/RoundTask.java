package dev.conc.p3;

/** The work one thread does in one round. */
@FunctionalInterface
public interface RoundTask {
    void run(int thread, int round) throws Exception;
}
