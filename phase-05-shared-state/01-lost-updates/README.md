# 01 Lost updates

## The problem
Break it first. `UnsafeCounter.increment()` is `value++`. That is three steps (read the value, add one, write it back), and two threads can both read the same value, so one increment is lost. You write a small harness that runs many threads against shared, unprotected state and reports how much was **expected** against how much was **actually counted**.

## What you implement
`RaceHarness` in `src/main/java/dev/conc/p5/` (`RaceResult(expected, actual)` is given, with `lost() = expected - actual`):
1. `countWithoutSync(int threads, int incrementsPerThread)`: start `threads` threads. Each calls `UnsafeCounter.increment()` `incrementsPerThread` times on **one shared counter**. Join them all. Return `expected = threads * incrementsPerThread`, `actual = counter.get()`.
2. `recordWithoutSync(int threads, List<Event> events)`: split `events` into `threads` slices (any split that covers every event exactly once). Each thread records its slice into **one shared `UnsafeProfileStore`** (a plain `HashMap` and a plain counter). Join them all. Return `expected = events.size()`, `actual = store.totalEvents()`.

## Rules
- Use `Thread.start` and `join`. No executors yet.
- Do **not** add any synchronization: the point is to see the race.
- A worker that throws (a plain `HashMap` under concurrent writes can) is part of the finding: do not let it hide the run, and still return a result.

## Example
```java
RaceHarness.countWithoutSync(1, 100_000);
   // RaceResult(expected=100000, actual=100000)   one thread loses nothing

RaceHarness.countWithoutSync(4, 1_000_000);
   // RaceResult(expected=4000000, actual=<some smaller number>)
   // actual is never above expected, and usually differs on every run
```
How much is lost depends on your CPU and the JIT; recording it over ten runs is the experiment.

## Run
`mvn -q test -Dtest='C01*'` (needs a machine with more than one core; if the loss test fails on a quiet machine, run it again).

## Done when
All `C01*` tests pass and `../results/<date>-lost-updates.md` exists.

## Then, for the post (write `../results/<date>-lost-updates.md`)
- Hypothesis first: what total do you expect from 4 threads times 1,000,000 increments, and by how much do you expect it to miss?
- Run it ten times. Record each total and the spread.
- Try 1, 2, 4, 8 threads. Where does the loss start?
- Look at what the store lost: only `totalEvents`, or also per-user counts? Did a worker throw?

## Think about
`value++` is one line of Java. What are the bytecode steps? (`javap -c`)
