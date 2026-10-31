# 01 Virtual threads

## The problem
You have thousands of tasks that mostly **wait** (a database call, a remote service). One platform (OS) thread per task is too heavy: each reserves stack memory and the OS scheduler struggles with tens of thousands of them. A **virtual thread** is a cheap JVM-managed thread: when it blocks, the JVM parks it and the underlying OS thread (the *carrier*) runs another virtual thread. You run ten thousand blocking tasks at once, one virtual thread each, and compare with a fixed pool of platform threads.

## What you implement
`FanOut.runAll(List<Callable<T>> tasks)` returning `List<Outcome<T>>` in `src/main/java/dev/conc/p12/`. `Outcome<T>(T value, Throwable error)` is given, and so is `PlatformFanOut` (the same job on a fixed pool, for the comparison).

## Rules
- Run each task on its **own virtual thread**: `Executors.newVirtualThreadPerTaskExecutor()`, closed with try-with-resources.
- Return the outcomes **in the order of the tasks**.
- A task that throws gives `Outcome(null, the exception it threw)`: the exception itself, **not** an `ExecutionException`. The other tasks are not affected.
- Return only after **every** task has finished.

## Example
```java
List<Callable<Integer>> tasks = List.of(
    () -> 1,
    () -> { throw new IllegalStateException("x"); },
    () -> { Thread.sleep(100); return 3; });
FanOut.runAll(tasks)
// [Outcome(1, null), Outcome(null, IllegalStateException("x")), Outcome(3, null)]
```
10 000 tasks that each `Thread.sleep(100)`: finishes in roughly 100 ms plus overhead on virtual threads. On a pool of 50 platform threads the same job needs at least `10000 / 50 x 100 ms` = 20 s.

## Run
`mvn -q test -Dtest='C01*'`. The test also times it against the given `PlatformFanOut`.

## Done when
All `C01*` tests pass and `../results/<date>-virtual-vs-platform.md` exists.

## Think about
- Why does a virtual thread that blocks in `sleep`, a socket read or a `BlockingQueue.take` not tie up an OS thread? What does the JVM do when it blocks, and what is a *carrier thread*?
- Which of your Phase 4 and 8 rules still apply (bounding in-flight work with credits)? Ten thousand cheap threads can still overload a database with ten thousand connections.
- Virtual threads do not make CPU-bound code faster. Run `cpuBurn` on them and on a pool of `nproc` threads: what do you see?
- In JDK 21 a `synchronized` block that blocked *pinned* the carrier; JEP 491 (JDK 24) removed that for monitors. What still pins (native frames, some `Object.wait` cases)? How do you find it (`-Djdk.tracePinnedThreads`, JFR `VirtualThreadPinned`)?
- Experiment: challenge 1 against `PlatformFanOut` for 100, 1,000, 10,000 tasks; the time, `jcmd <pid> Thread.print | wc -l`, and the memory.
