# Phase 10: Graceful shutdown

Starting threads is easy; stopping them without losing work is not. This phase is about the end of the life of a pipeline: closing a pool in two phases, workers that stop cleanly on an interrupt, and a pipeline that never loses an event it said it accepted.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-close-a-pool`: `shutdown`, wait, then `shutdownNow` | `mvn -q test -Dtest='C01*'` |
| 2 | `02-interruptible-worker`: stop on interrupt, lose nothing, keep the flag | `mvn -q test -Dtest='C02*'` |
| 3 | `03-drainable-pipeline`: accepted means processed, even when `close` races with `submit` | `mvn -q test -Dtest='C03*'` |

`Event` and `EventType` are given. Your code is in `src/main/java/dev/conc/p10/`; replace each `TODO`.

**Read first (once):** JCIP ch. 7 (cancellation and shutdown), all of it; the `ExecutorService` javadoc (the `shutdownAndAwaitTermination` example at the top of the class description, and `close()` since JDK 19); Brian Goetz's article *Java theory and practice: Dealing with InterruptedException*. Check the references.

**Evidence:** challenge 3 is the experiment. Run the race test's idea yourself with 4 submitters and `close` at a random moment, 1,000 times, and count how many events are lost with (a) a plain `volatile boolean closed` check followed by `put`, and (b) your lock-based version. Write the counts and the reason in `../results/<date>-shutdown-race.md`.
