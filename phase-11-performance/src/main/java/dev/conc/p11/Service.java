package dev.conc.p11;

/** Something whose latency we measure: one request. */
@FunctionalInterface
public interface Service {
    void call() throws Exception;
}
