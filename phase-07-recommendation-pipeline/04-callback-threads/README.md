# 04 Callback threads

## The problem
When you attach a step to a future, **which thread runs it**? With `thenApply` it is whichever thread completes the future (or the caller, if it is already complete). In the simulator, that is `p7-sim`, the one thread that also fires every timer. In a real client library it is often a single I/O thread. A heavy CPU step done there delays every other request. You attach the heavy step so it runs on a separate CPU pool.

## What you implement
`RankStage(Executor cpuPool, UnaryOperator<List<String>> heavy)` with `CompletableFuture<List<String>> rank(CompletableFuture<List<String>> candidates)`. `heavy` is the expensive step (for example sorting by a costly score).

## Rules
- The `heavy` step runs on `cpuPool`: never on `p7-sim`, and never on the common pool. This holds even if `candidates` is already complete when you receive it.
- `rank` returns a future of `heavy`'s result.
- If `heavy` throws, the returned future fails (its cause is that exception).
- If `candidates` fails, `heavy` is never called and the returned future fails.

## Example
```java
var stage = new RankStage(cpuPool /* threads named "cpu-..." */, list -> sort(list));
stage.rank(Simulator.after(10, () -> List.of("b", "a")))
// heavy runs on a "cpu-N" thread, result [a, b]

// timer check: while eight 25 ms heavy steps are running, a 30 ms timer scheduled on p7-sim
// must still fire close to 30 ms (a test allows under 80 ms). If heavy ran on p7-sim,
// the timer would fire up to ~200 ms late.
```

## Run
`mvn -q test -Dtest='C04*'`. One test starts a 30 ms timer while eight 25 ms CPU-heavy steps are running.

## Done when
All `C04*` tests pass and `../results/<date>-callback-threads.md` has the lateness numbers.

## Think about
- A non-async callback (`thenApply`) runs on whichever thread completes the future, or on the caller if it is already complete. In a real client library that thread is often a single I/O thread. What happens to every other request when you do 25 ms of CPU work on it?
- `thenApplyAsync` without an executor uses `ForkJoinPool.commonPool()`. Why is that also a poor choice for blocking or heavy work?
- Which pools in a real service should be separate: the I/O threads, the CPU work, the blocking calls? What does each need to be sized for? (Phase 4 and 11)
