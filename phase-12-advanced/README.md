# Phase 12: Advanced concurrency

Only the tools that solve a problem you already hit in Phases 1 to 11: virtual threads for thousands of waiting tasks, fork/join for divide-and-conquer, `StampedLock` for read-heavy state, a limiter and `ScopedValue` for bounded concurrency and request context, then the scheduler Trino uses to run more splits than threads (a time-sliced executor with a multilevel queue), and finally three network servers (blocking, NIO selector, virtual threads) to see how the models differ.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-virtual-threads`: ten thousand blocking tasks, one virtual thread each | `mvn -q test -Dtest='C01*'` |
| 2 | `02-fork-join`: per-user scores by divide and conquer | `mvn -q test -Dtest='C02*'` |
| 3 | `03-stamped-lock`: optimistic reads with a fallback | `mvn -q test -Dtest='C03*'` |
| 4 | `04-limiter-and-scoped-value`: a semaphore limiter and a request id in a `ScopedValue` | `mvn -q test -Dtest='C04*'` |
| 5 | `05-time-sliced-scheduler`: a fixed pool running many splits a quantum at a time | `mvn -q test -Dtest='C05*'` |
| 6 | `06-multilevel-queue`: short work first, long work never starved | `mvn -q test -Dtest='C06*'` |
| 7 | `07-blocking-server`: thread per connection | `mvn -q test -Dtest='C07*'` |
| 8 | `08-nio-server`: one selector thread for every connection | `mvn -q test -Dtest='C08*'` |
| 9 | `09-virtual-thread-server`: thread per connection, with virtual threads | `mvn -q test -Dtest='C09*'` |

`Event` and `EventType` are given complete here. The line protocol, the sink, the `BurnSplit` job and the `Outcome`/`Snapshot` types are given too. Your code is in `src/main/java/dev/conc/p12/`; replace each `TODO`. Challenges 5 and 6 are independent of each other; once both pass, wire the queue into the executor yourself as an extension (see challenge 6).

**Read first (once):** the JEPs on virtual threads (444), scoped values (506) and the JDK 25 notes on synchronized and pinning (491); `StampedLock` javadoc; Doug Lea, *A Java Fork/Join Framework*; the Trino source for `TaskExecutor`, `PrioritizedSplitRunner`, `MultilevelSplitQueue` and `Driver` (open them after challenge 5, and compare); OSTEP's event-based concurrency chapter and the I/O chapters; Beej's guide or `man 7 epoll` for what `Selector` does underneath; the Netty user guide for the event-loop model. Check the references (the JEP numbers are from memory).

**Evidence:** the experiments of this phase:
1. `../results/<date>-virtual-vs-platform.md`: challenge 1 against `PlatformFanOut` for 100, 1,000, 10,000 tasks; the time, `jcmd <pid> Thread.print | wc -l`, and the memory.
2. `../results/<date>-time-slicing-fairness.md`: challenge 5 with quantum 1 ms, 10 ms, 100 ms and "run to completion" (a huge quantum), 1 long and 20 short splits, short-split latency per quantum.
3. `../results/<date>-server-models.md`: reuse Phase 11: for challenges 7, 8 and 9 measure p50/p99/p99.9 from your open-loop generator at 1,000 and 10,000 connections, thread count (`jcmd`/`ps -L`), and what happens when 5% of the clients are slow. Say which design you would build for which workload, and why.
