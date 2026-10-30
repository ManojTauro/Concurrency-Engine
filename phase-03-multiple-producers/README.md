# Phase 3: Multiple producers

Many producers feed one consumer. The new problems are termination (who says "done"?), coordination between threads that must move together, and the queue itself under contention.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-multi-producer-pipeline`: N producers, one queue, stop exactly when the last one finishes | `mvn -q test -Dtest='C01*'` |
| 2 | `02-round-runner`: threads in lock-step rounds with a barrier, and a failure that must not hang the rest | `mvn -q test -Dtest='C02*'` |
| 3 | `03-two-lock-queue`: an unbounded linked queue with one lock at each end | `mvn -q test -Dtest='C03*'` |

`Event` and `EventType` are given complete here. Your code is in `src/main/java/dev/conc/p3/`; replace each `TODO`.

**Read first (once):** JCIP ch. 5 (blocking queues, synchronizers: latches, barriers) and the first half of ch. 15; Michael and Scott, *Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Queue Algorithms* (1996) for the two-lock queue; OSTEP condition variables. Check the references.

**Evidence:** challenge 3 is the experiment. Time your `TwoLockQueue` against `LinkedBlockingQueue` and `ArrayBlockingQueue` with 1, 2, 4 and 8 producers and consumers, and write what you see and why in `../results/<date>-queue-contention.md`.
