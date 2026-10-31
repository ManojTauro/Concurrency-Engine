# 02 Executor config

## The problem
`ThreadPoolExecutor` is the pool everyone uses, and its growth rule surprises almost everyone. You configure one from a small spec and prove, with tests, how it really behaves: when it creates threads, what it does when it is full, and when idle threads die.

## What you implement
`PoolFactory.create(PoolSpec spec)` in `src/main/java/dev/conc/p4/`, returning a `ThreadPoolExecutor`. `PoolSpec` (given) has: `namePrefix`, `core`, `max`, `queueCapacity`, `keepAliveMillis`, `rejection` (`ABORT`, `CALLER_RUNS`, `DISCARD_OLDEST`), `prestart`.

## Rules
- Work queue: a bounded `ArrayBlockingQueue` of `queueCapacity`.
- Worker threads are named `namePrefix + "-" + n`, `n` counting up from 1 (`worker-1`, `worker-2`, ...).
- `rejection` selects what happens when the queue is full **and** the pool is at `max`:
  - `ABORT`: throw `RejectedExecutionException`
  - `CALLER_RUNS`: the submitting thread runs the task itself
  - `DISCARD_OLDEST`: drop the task that has waited longest in the queue, then retry the new one
- `prestart = true`: core threads exist immediately (`getPoolSize() == core` before any task). `false`: threads are created on demand.
- Idle threads **above the core size** disappear after `keepAliveMillis`; the pool shrinks back to `core`.
- `core`, `max`, keep-alive and queue capacity are visible through the executor's getters.

## Example
Spec: core 2, max 4, queue 2, all tasks block forever. Submit tasks one by one and watch `getPoolSize()` and the queue:
```
task 1 -> pool 1 thread, queue 0
task 2 -> pool 2 threads, queue 0
task 3 -> pool 2 threads, queue 1      (no new thread: the queue has room)
task 4 -> pool 2 threads, queue 2      (queue now full)
task 5 -> pool 3 threads, queue 2      (only now does it grow beyond core)
task 6 -> pool 4 threads, queue 2      (max reached)
task 7 -> ABORT: RejectedExecutionException
         CALLER_RUNS: the submitting thread runs task 7 itself
         DISCARD_OLDEST: queued task 3 (waited longest) is dropped, task 7 takes its place
```
This is the "famous surprise": the pool grows past `core` only when the queue is full.

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about (this is the famous surprise)
- With core 2, max 4 and a queue of 2, the third task does NOT create a third thread. Why? When does the pool grow beyond the core size?
- What does that mean for an unbounded queue and `max`? Why is `max` then useless?
- `CallerRunsPolicy` slows the submitter down. Why is that sometimes exactly what you want (hint: Phase 8)?
- What does `DiscardOldest` lose, and when would you accept that?
