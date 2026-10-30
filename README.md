# Concurrency Engine

A real-time e-commerce behaviour and recommendation engine, built phase by phase in plain Java to learn concurrency from problems you hit, not from isolated exercises. Source design: `~/Desktop/sources/concurrency-engine-tracker.html` (13 phases; two gaps added). JDK 25, Maven, one module per phase added when the phase starts.

Why it exists: I read theory on and off and it did not stick. The goal is to build systems like MiniTrino (`~/Desktop/learning/systems/query-engines/MiniTrino`) on my own. Each phase has a "break it on purpose" step and a measurement, and the measurement is the evidence for one weekly X post (see the `/jvm` skill in the vault).

Self-managed: no hours in the roadmap lanes, no dated tasks. Phase 13 (Kafka and Redis) is gated behind the main roadmap's Kafka phase.

## Evidence convention

Every experiment gets `results/<YYYY-MM-DD>-<topic>.md` from `results/_TEMPLATE.md`: hypothesis first, exact command and flags, JDK and CPU, raw unedited output, spread over repeated runs, what surprised me, and when it helps and when it does not. No claim goes into a post unless it is in one of these notes.

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
| 11 Performance experiments | Throughput and p95 vs worker count, 1 to 32 | JCIP ch. 8.2 and 11; async-profiler README; JMH samples; Shipilev, *JVM Anatomy Quarks* (shipilev.net/jvm/anatomy-quarks/); the Universal Scalability Law (Gunther) |
| 12 Advanced | Virtual threads, `ForkJoinPool`, `StampedLock`, structured concurrency. **Add:** a cooperative time-sliced scheduler (Trino's `Driver` / `TaskExecutor`) | JEPs 444, 491, 505, 506 (openjdk.org/jeps/); Doug Lea, *A Java Fork/Join Framework*; `StampedLock` javadoc; Trino source: `TaskExecutor`, `PrioritizedSplitRunner`, `MultilevelSplitQueue`, `Driver` |
| 13 Distributed (gated) | Kafka + Redis | Wait for the main roadmap's Kafka phase |

## Status

Phases 1 and 5 are generated (skeletons, tests, READMEs); the other phases are not yet. Progress: `.learn-by-doing/state.json`, dashboard at `.learn-by-doing/dashboard.html`.

Start: `cd phase-01-domain-model/01-event-model`, read its README, edit `../src/main/java/dev/conc/p1/`, then `mvn -q -f ../pom.xml test -Dtest='C01*'`. Phase 5 does not depend on Phase 1 (it ships its own complete domain classes), so Phase 5's first experiment (`05 / 01-lost-updates`) can be done first if you want the first post sooner.

Validated: with a reference solution (kept out of this repo) all 24 Phase 1 tests and all 46 Phase 5 tests pass, and the Phase 5 contract tests fail against the deliberately broken `UnsafeProfileStore`.
