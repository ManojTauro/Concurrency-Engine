package dev.conc.p5;

import java.time.Duration;

/**
 * Given. Run it three ways and keep the output in results/:
 *   java -cp target/classes dev.conc.p5.VisibilityDemoMain
 *   java -Xint -cp target/classes dev.conc.p5.VisibilityDemoMain
 *   java -XX:+PrintCompilation -cp target/classes dev.conc.p5.VisibilityDemoMain
 */
public final class VisibilityDemoMain {
    public static void main(String[] args) {
        Duration runFor = Duration.ofMillis(500);
        Duration giveUp = Duration.ofSeconds(3);
        for (int i = 1; i <= 3; i++) {
            long t0 = System.nanoTime();
            boolean plain = VisibilityDemo.plainFlagStops(runFor, giveUp);
            long plainMs = (System.nanoTime() - t0) / 1_000_000;
            t0 = System.nanoTime();
            boolean vol = VisibilityDemo.volatileFlagStops(runFor, giveUp);
            long volMs = (System.nanoTime() - t0) / 1_000_000;
            System.out.printf("run %d: plain stopped=%s (%d ms), volatile stopped=%s (%d ms)%n", i, plain, plainMs, vol, volMs);
        }
    }
}
