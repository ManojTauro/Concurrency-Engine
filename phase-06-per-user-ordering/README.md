# Phase 6: Per-user ordering

Parallel across users, ordered within one user. Two events of the same user must never be applied out of order, but two different users should never wait for each other. A plain thread pool breaks the first rule; one big lock breaks the second.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-lane-executor`: N single-thread lanes; a key always goes to the same lane | `mvn -q test -Dtest='C01*'` |
| 2 | `02-serial-executor`: per-key order on a shared pool, with no thread per key, and fairness | `mvn -q test -Dtest='C02*'` |
| 3 | `03-lane-profile-store`: the single-writer idea, a profile store with no locks | `mvn -q test -Dtest='C03*'` |

`Event`, `EventType` and `UserBehaviourProfile` are given complete here. `NaivePoolStore` (the design that breaks ordering) and `OrderingDemoMain` are given too. Your code is in `src/main/java/dev/conc/p6/`; replace each `TODO`.

**Read first (once):** JCIP ch. 5 and 6 (the section on executors and ordering); the LMAX Disruptor paper (lmax-exchange.github.io/disruptor/) for the single-writer principle; Guava's `SequentialExecutor` (read its source after challenge 2: it is the same idea); OSTEP concurrency bugs (order violations). Check the references.

**Evidence:** run `OrderingDemoMain` first with the naive store and write what you see in `../results/<date>-ordering-violations.md`: hypothesis first (how many events out of order, with 8 threads and one hot user?), then ten runs. After challenge 3, add the lane store to the same table, with throughput as well.
