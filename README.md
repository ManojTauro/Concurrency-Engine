# Concurrency Engine

A real-time e-commerce behaviour and recommendation engine, built phase by phase in plain Java to learn concurrency from problems you hit, not from isolated exercises. Source design: `~/Desktop/sources/concurrency-engine-tracker.html` (13 phases; two gaps added). JDK 25, Maven, one module per phase added when the phase starts.

Why it exists: I read theory on and off and it did not stick. The goal is to build systems like MiniTrino (`~/Desktop/learning/systems/query-engines/MiniTrino`) on my own. Each phase has a "break it on purpose" step and a measurement, and the measurement is the evidence for one weekly X post (see the `/jvm` skill in the vault).

Self-managed: no hours in the roadmap lanes, no dated tasks. Phase 13 (Kafka and Redis) is gated behind the main roadmap's Kafka phase.

## Evidence convention

Every experiment gets `results/<YYYY-MM-DD>-<topic>.md` from `results/_TEMPLATE.md`: hypothesis first, exact command and flags, JDK and CPU, raw unedited output, spread over repeated runs, what surprised me, and when it helps and when it does not. No claim goes into a post unless it is in one of these notes.

## How a challenge README is laid out

Every challenge README has the same sections, so you know where to look:

1. **The problem**: what is being solved, in plain words, and why it matters for concurrency.
2. **What you implement**: the file, the class and the method signatures you fill in (everything else is given).
3. **Rules**: the exact contract: names, exceptions, ordering, limits. If a rule is not here or in the Javadoc above the method, the tests do not require it.
4. **Example**: small concrete inputs and outputs or a timeline, so you can check your understanding before you write code. Numbers marked as shape (for example "a smaller number") depend on your machine.
5. **Run** and **Done when**: the one command, and what counts as finished (including any `results/` note).
6. **Think about**: questions to answer in your notes; they are not tested.

## Phases and what to read first

Links and chapter numbers are from memory: check them before relying on them. JCIP = Goetz, *Java Concurrency in Practice*; OSTEP = *Operating Systems: Three Easy Pieces* (pages.cs.wisc.edu/~remzi/OSTEP/).

Before Phase 1 (once):
- JCIP ch. 1–3 (thread safety, sharing objects, visibility).
- OSTEP concurrency intro, thread API, locks, condition variables, semaphores, common concurrency bugs.
- JLS ch. 17, Threads and Locks (docs.oracle.com/javase/specs), at least 17.4, the memory model.

| Phase | Build | Read before |
|---|---|---|
| 1 Domain model | Events, users, products; no threads yet | JCIP ch. 3 on immutability and safe publication |
| 2 Producer / consumer | One producer, one consumer, a blocking queue | JCIP ch. 5.3 (`BlockingQueue`); `java.util.concurrent` package docs |
| 3 Multiple producers | Several producers into one queue | JCIP ch. 5; OSTEP condition variables |
| 4 Worker pool | A pool of consumers; pool sizing | JCIP ch. 6 and 8 (executors, sizing); `ThreadPoolExecutor` javadoc; Doug Lea, *The java.util.concurrent Synchronizer Framework* (gee.cs.oswego.edu/dl/papers/aqs.pdf) |
| 5 Shared user state | **Break:** races, a stop flag that never stops. Fix with `synchronized`, `ConcurrentHashMap`, `LongAdder`, `ReentrantLock`. **Add:** JMM + `jcstress` | JCIP ch. 2, 3, 15; Shipilev, *Java Memory Model Pragmatics* (shipilev.net/blog/2014/jmm-pragmatics/); JSR-133 Cookbook (gee.cs.oswego.edu/dl/jmm/cookbook.html); `jcstress` repo samples (github.com/openjdk/jcstress); JMH samples (github.com/openjdk/jmh); `LongAdder` / `Striped64` javadoc and source |
| 6 Per-user ordering | Parallel across users, ordered within a user | JCIP ch. 5; the LMAX Disruptor paper (lmax-exchange.github.io/disruptor/) for the single-writer idea; OSTEP concurrency bugs |
| 7 Recommendation pipeline | Stages, futures, composition | JCIP ch. 5.5 and 6.3 (`Future`); `CompletableFuture` javadoc |
| 8 Backpressure | **Break:** producer faster than consumer; bounded queues | JCIP ch. 8.3 (saturation policies); Reactive Streams spec on backpressure (reactive-streams.org); Little's law |
| 9 Failure handling | Random failures, retries, dead letter | JCIP ch. 7.3 (abnormal thread termination); Nygard, *Release It!* (retries, timeouts, circuit breakers) |
| 10 Graceful shutdown | Drain in-flight work | JCIP ch. 7; `ExecutorService.close()` javadoc (JDK 19+) |
| 11 Performance experiments | Throughput and p95 vs worker count, 1 to 32. **Add:** HdrHistogram (p99, p99.9, max), an open-loop load generator (coordinated omission), find the knee under rising arrival rate | JCIP ch. 8.2 and 11; async-profiler README; JMH samples; Shipilev, *JVM Anatomy Quarks* (shipilev.net/jvm/anatomy-quarks/); the Universal Scalability Law (Gunther); Gil Tene's talk *How NOT to Measure Latency* and the HdrHistogram README (github.com/HdrHistogram/HdrHistogram) |
| 12 Advanced | Virtual threads, `ForkJoinPool`, `StampedLock`, structured concurrency. **Add:** a cooperative time-sliced scheduler (Trino's `Driver` / `TaskExecutor`); a TCP network front (thread-per-connection, NIO selector, virtual threads) with slow-client and backpressure breaks | JEPs 444, 491, 505, 506 (openjdk.org/jeps/); Doug Lea, *A Java Fork/Join Framework*; `StampedLock` javadoc; Trino source: `TaskExecutor`, `PrioritizedSplitRunner`, `MultilevelSplitQueue`, `Driver`; OSTEP I/O and event-based concurrency chapters; `java.nio` `Selector` javadoc; JEP 444 on virtual threads and blocking I/O |
| 13 Distributed (gated) | Kafka + Redis | Wait for the main roadmap's Kafka phase |

## Status

Phases 1 to 12 are generated: 12 Maven modules and 50 challenges (skeleton, tests, README each). Phase 13 (Kafka and Redis) is not generated: it is gated behind the main roadmap's Kafka phase. Progress: `.learn-by-doing/state.json`, dashboard at `.learn-by-doing/dashboard.html`.

| Phase | Module | Challenges |
|---|---|---|
| 1 Domain model | `phase-01-domain-model` | 3 |
| 2 Producer / consumer | `phase-02-producer-consumer` | 3 |
| 3 Multiple producers | `phase-03-multiple-producers` | 3 |
| 4 Worker pool | `phase-04-worker-pool` | 4 |
| 5 Shared user state | `phase-05-shared-state` | 6 |
| 6 Per-user ordering | `phase-06-per-user-ordering` | 3 |
| 7 Recommendation pipeline | `phase-07-recommendation-pipeline` | 4 |
| 8 Backpressure | `phase-08-backpressure` | 4 |
| 9 Failure handling | `phase-09-failure-handling` | 4 |
| 10 Graceful shutdown | `phase-10-graceful-shutdown` | 3 |
| 11 Performance experiments | `phase-11-performance` | 4 |
| 12 Advanced | `phase-12-advanced` | 9 |

Work in order. Start: `cd phase-01-domain-model/01-event-model`, read its README, edit `../src/main/java/dev/conc/p1/`, then `mvn -q -f ../pom.xml test -Dtest='C01*'`. Every phase folder has a `README.md` with its challenge table, the reading and the evidence you should write down. Each phase is self-contained (it ships its own copy of `Event` and `EventType`), so a later phase never depends on your solution to an earlier one. Phase 5's first experiment (`05 / 01-lost-updates`) can be done early if you want a first post sooner.

Tests in phases 2 to 12 have a default timeout of 60 s each (`src/test/resources/junit-platform.properties`), so an unfinished or deadlocked solution fails instead of hanging. Timing-based tests (phases 3, 7, 8, 11, 12) need several free cores and a quiet machine: if one fails, run it again before suspecting your code.

Validated: with a reference solution for every challenge (kept out of this repo) all tests of phases 1 to 12 pass, and with the unimplemented skeletons they fail at the `TODO`s.
