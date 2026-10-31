# 02 Serial executor

## The problem
Lanes (challenge 1) need one thread per lane, and a busy lane cannot lend its thread to an idle one. Guava's `SequentialExecutor` makes the same promise with **no dedicated threads**: each key has its own queue of tasks, and a job on a shared pool drains that queue only while it has work. One key runs at most one task at a time (so order is kept); different keys run in parallel on whatever pool threads are free. The hard parts: starting a drain job exactly once, cleaning up when a key goes idle, and not letting one key with a huge backlog hog the pool.

## What you implement
`KeyedSerialExecutor` in `src/main/java/dev/conc/p6/`:
- `new KeyedSerialExecutor(Executor pool, int maxBatch)`
- `void execute(String key, Runnable task)`
- `int activeKeys()`: number of keys that currently have queued or running work

## Rules
- Tasks with the same key run one at a time, in submission order: never two at once, and a task never starts before the previous one of that key has **finished**.
- Tasks with different keys run in parallel on the shared pool.
- No thread per key and no thread of your own: use only the pool you were given.
- Memory for a key exists only while it has work: `activeKeys()` is `0` when everything is done.
- A task that throws must not stop later tasks of the same key.
- **Fairness:** one pool job runs at most `maxBatch` tasks of a key, then puts itself at the back of the pool's queue, so a key with a huge backlog cannot starve other keys.
- `maxBatch < 1` throws `IllegalArgumentException`.
- The pool is **not owned** by this class: do not shut it down. It must stay open until the keys are idle, because a job hands its key back to the pool.
- Works with an inline executor (`Runnable::run`) too.

## Example
Pool of 4 threads, `maxBatch = 10`:
```
execute("a", A1) ... execute("a", A300)    // 300 tasks of key "a"
execute("b", B1)
```
Without `maxBatch`, one job could run A1..A300 back to back and B1 would wait for all of them. With `maxBatch = 10`, the job runs A1..A10, requeues itself at the back, and B1 gets a turn long before A300 (a test requires B1 to finish before 100 of the 300 "a" tasks).
When every task has finished, `activeKeys() == 0`.

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about
- Which operation must be atomic: "add a task and decide whether to start a drain job"? What happens if two threads add the first two tasks of a new key at the same moment?
- How do you remove the key's state exactly when its last task finishes, without losing a task that arrives at that same moment?
- What does `maxBatch` buy, and what does it cost? What goes wrong with `maxBatch` = 1? With `Integer.MAX_VALUE`?
- Compare with challenge 1: when would you choose lanes, when this?
