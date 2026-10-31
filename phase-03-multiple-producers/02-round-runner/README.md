# 02 Round runner

## The problem
Some work runs in lock-step **rounds**: all threads do step 1, wait for each other, then all do step 2, and so on (a simulation tick, a map phase then a reduce phase). You build a runner that guarantees "nobody starts round r+1 before everybody finished round r". The hard part is a thread that **fails** while the others are waiting for it at the barrier: they must be released, not left hanging forever.

## What you implement
`RoundRunner.run(int threads, int rounds, RoundTask task)` in `src/main/java/dev/conc/p3/`. `RoundTask` is `void run(int thread, int round) throws Exception`. Use a `CyclicBarrier` (or a `Phaser`).

## Rules
- Start `threads` threads, named `p3-round-0`, `p3-round-1`, ...
- Thread `t` calls `task.run(t, r)` exactly once for each round `r = 0 .. rounds-1`.
- **No thread starts round `r+1` before all threads have finished round `r`.**
- `run` returns when all rounds are done, and no `p3-` thread is alive.
- If any task throws, `run` throws `RoundFailedException` whose cause is the **first** exception, promptly. The other threads must not wait for the failed one forever (this includes a failure in round 0, before the others reach the barrier).
- `threads < 1` or `rounds < 0` throws `IllegalArgumentException`. `rounds = 0` returns immediately without calling the task.

## Example
3 threads, 2 rounds. A valid timeline (time goes down):
```
t0: run(0,0) ........ barrier
t1: run(1,0) ............ barrier        <- t1 is slow
t2: run(2,0) .. barrier
                          ^ all three arrive, barrier opens
t0: run(0,1) ... t1: run(1,1) ... t2: run(2,1) ...
```
`task.run(0, 1)` can never begin before `task.run(1, 0)` has returned.

Failure: 6 threads, 20 rounds, and `task` throws `IllegalStateException("boom")` when `thread == 2 && round == 3`:
```
run(...) throws RoundFailedException, getCause() is IllegalStateException("boom")
it returns within a few seconds (not after the other 5 threads wait forever at round 3's barrier)
no p3-round-* thread is left
```

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about
- One thread throws in round 3. The other five are waiting at the barrier for it. What do they see, and how do you tell them? Does `barrier.reset()` solve it? When does it not?
- What if the thread fails in round 0, before the others even reach the barrier?
- A `CountDownLatch` cannot be reused for the next round. A `CyclicBarrier` can. What does `Phaser` add?
