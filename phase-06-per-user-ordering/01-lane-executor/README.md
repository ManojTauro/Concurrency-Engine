# 01 Lane executor

## The problem
Events of one user must be applied **in order** (a purchase must not be applied before the add-to-cart that came first), but different users should be processed **in parallel**. A plain thread pool breaks the first rule: two events of one user can sit in the queue and be picked up by two workers. One big lock breaks the second. The fix: a fixed number of **lanes**. Each lane is one thread with its own queue. A user's key always maps to the same lane, so that user's tasks run one after another, in submission order.

## What you implement
`LaneExecutor` in `src/main/java/dev/conc/p6/`:
- `new LaneExecutor(int lanes)`: `lanes < 1` throws `IllegalArgumentException` (given)
- `int laneOf(String key)`: **given**: `floorMod(key.hashCode(), lanes)`
- `void execute(String key, Runnable task)`
- `void shutdown()`
- `boolean awaitTermination(long timeout, TimeUnit unit)`

## Rules
- Lane threads are named `p6-lane-0`, `p6-lane-1`, ... (one per lane).
- A task goes to `laneOf(key)`'s thread. A key therefore always runs on the same named thread.
- Tasks with the same key run one after another, in submission order. Tasks of keys in different lanes run in parallel.
- A task that throws must not stop its lane.
- `execute` after `shutdown()` throws `RejectedExecutionException`.
- `shutdown()` lets queued tasks finish. `awaitTermination` returns `true` once the lane threads have ended, `false` if the time ran out.

## Example
`new LaneExecutor(3)`, and `laneOf("user-7") == 1`:
```
execute("user-7", A); execute("user-7", B); execute("user-7", C);
   // A, B, C all run on thread "p6-lane-1", in exactly that order
execute("user-9", X);   // if laneOf("user-9") == 2, X runs on "p6-lane-2" at the same time as A/B/C
execute("user-7", boom) // throws inside the task: lane 1 keeps running the next tasks
shutdown(); execute("k", Z)   // RejectedExecutionException
```
Two keys in different lanes must be able to run at the same time (a test makes two tasks wait for each other, so they only finish if they really overlap).

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass.

## Think about
- Why does one single-thread lane per key group give you ordering for free? What would break it if a lane had two threads?
- Hash partitioning is uneven. What happens to a lane when one user (a bot, a big merchant) produces most of the events? What can you do about it without losing ordering?
- Two *different* users share a lane. Is that a correctness problem? A performance one?
