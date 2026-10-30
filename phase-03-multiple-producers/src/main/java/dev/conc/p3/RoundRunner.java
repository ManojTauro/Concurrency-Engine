package dev.conc.p3;

/**
 * Challenge 2. Run {@code threads} threads for {@code rounds} rounds. Every thread runs
 * task.run(thread, round) once per round, and NO thread starts round r + 1 before ALL threads have
 * finished round r. Use a CyclicBarrier or a Phaser.
 *
 *  - threads are named "p3-round-0", "p3-round-1", ...; none is alive when run() returns;
 *  - if any task throws, run() throws RoundFailedException (cause = the first exception) promptly:
 *    the other threads must not wait for the failed one forever;
 *  - threads < 1 or rounds < 0 throws IllegalArgumentException; rounds = 0 returns immediately.
 */
public final class RoundRunner {

    private RoundRunner() {
    }

    public static void run(int threads, int rounds, RoundTask task) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
