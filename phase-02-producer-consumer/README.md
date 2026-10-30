# Phase 2: Producer / consumer

One thread makes events, another handles them, and a bounded buffer sits between them. You build the buffer by hand twice (so you see what `BlockingQueue` is doing for you), then use the real one and meet the shutdown problem.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-wait-notify-buffer`: a bounded buffer from `synchronized`, `wait`, `notifyAll` | `mvn -q test -Dtest='C01*'` |
| 2 | `02-condition-buffer`: the same with `ReentrantLock` and two `Condition`s, plus timeouts | `mvn -q test -Dtest='C02*'` |
| 3 | `03-pipeline`: producer, `ArrayBlockingQueue`, consumer, clean stop, failure that does not hang | `mvn -q test -Dtest='C03*'` |

`Event` and `EventType` are given complete here, so this phase does not depend on Phase 1. Your code is in `src/main/java/dev/conc/p2/`; replace each `TODO`.

**Read first (once):** JCIP ch. 5.3 and 14 (`BlockingQueue`, building your own state-dependent classes); OSTEP condition variables and the producer/consumer chapter; the `java.util.concurrent.BlockingQueue` javadoc. Check the references.

**Evidence:** challenge 1 is the first experiment worth a note. Replace `notifyAll` with `notify` in your buffer, run the many-producers test, and write down what happens in `../results/<date>-notify-vs-notifyall.md`.
