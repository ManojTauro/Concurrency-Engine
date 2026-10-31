# 01 Close a pool

## The problem
Stopping a thread pool is two steps, not one: first ask it to finish what it has (`shutdown`), wait a **grace period**, and only if that is not enough force it (`shutdownNow`, which interrupts running tasks and hands back the queued ones). Three things can happen: everything finishes in time; it does not and you must force it; or the thread that is waiting is itself interrupted. You write the one method that handles all three.

## What you implement
`GracefulShutdown.close(ExecutorService pool, Duration grace)` in `src/main/java/dev/conc/p10/`, returning `ShutdownReport(boolean clean, boolean terminated, List<Runnable> neverStarted)`.

## Rules
1. Call `pool.shutdown()` (no new tasks, queued ones still run) and wait up to `grace` for the pool to terminate. If it does: `ShutdownReport(true, true, empty list)`.
2. Otherwise call `pool.shutdownNow()`: the list it returns is `neverStarted`, and running tasks are interrupted. Wait **one more second (fixed)** for the pool to terminate, then return `ShutdownReport(false, pool.isTerminated(), neverStarted)`. A task that ignores its interrupt must not make `close` wait longer than that one second.
3. If the **calling thread** is interrupted while waiting: go to `shutdownNow` immediately, **set the calling thread's interrupt flag again** (do not swallow it), and return `ShutdownReport(false, pool.isTerminated(), neverStarted)` without further waiting.
- Calling it on a pool that is already shut down is fine.

## Example
Pool with 1 thread; `grace = 200 ms`:
```
idle pool                                       -> (clean=true, terminated=true, neverStarted=[])
5 quick queued tasks, all done within 200 ms    -> (true, true, [])  and all 5 ran
a running task of 10 s that honours interrupts, three tasks q1 q2 q3 queued behind it
   after 200 ms: shutdownNow -> the running task is interrupted
   -> (clean=false, terminated=true, neverStarted=[q1, q2, q3])    took about 200 ms
a running task that ignores interrupts (sleeps in a loop)
   -> (false, terminated=false, [...])    took about 100 ms grace + 1000 ms, then gives up
caller interrupted while waiting -> returns soon, report clean=false, and Thread.currentThread().isInterrupted() is true
```

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass.

## Think about
- `shutdown()` and `shutdownNow()` do different things to the queue and to running tasks. What exactly does each one do to a task that is *running*? To one that is *waiting*?
- `shutdownNow()` only sends interrupts. What kind of task survives it, and how long can your `close` stay blocked on such a task?
- If the thread calling `awaitTermination` is interrupted, what do you do with that interrupt? Why must you set the flag again?
- Which tasks in `neverStarted` do you now need to do something with (re-queue, log, persist)? In Phase 8 terms, whose responsibility are they?
