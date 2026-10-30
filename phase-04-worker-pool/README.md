# Phase 4: Worker pool

Many consumers instead of one. You build a pool by hand, then configure the real `ThreadPoolExecutor` and find out how it really grows, learn how many threads a pool should have, and make sure a failed task never disappears.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-simple-pool`: a fixed pool from threads and a bounded queue | `mvn -q test -Dtest='C01*'` |
| 2 | `02-executor-config`: `ThreadPoolExecutor` from a spec: growth order, rejection, keep-alive | `mvn -q test -Dtest='C02*'` |
| 3 | `03-pool-sizing`: sizing formulas and a throughput sweep | `mvn -q test -Dtest='C03*'` |
| 4 | `04-failed-tasks`: a pool that never loses a task failure | `mvn -q test -Dtest='C04*'` |

`Event` and `EventType` are given complete here. Your code is in `src/main/java/dev/conc/p4/`; replace each `TODO`.

**Read first (once):** JCIP ch. 6 and 8 (executors, sizing, configuring `ThreadPoolExecutor`); the `ThreadPoolExecutor` javadoc, especially "Queuing" and "Rejected tasks"; Doug Lea, *The java.util.concurrent Synchronizer Framework* (gee.cs.oswego.edu/dl/papers/aqs.pdf) for how the queues and locks underneath are built. Check the references.

**Evidence:** challenge 3 is the experiment. Run `Sweep` with `Workloads.cpuBurn` and with `Workloads.sleep` for 1, 2, 4, 8, 16, 32 workers on your machine, and write the table and an explanation in `../results/<date>-pool-sizing.md`. Phase 11 builds on it.
