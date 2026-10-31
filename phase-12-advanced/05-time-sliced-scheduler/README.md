# 05 Time-sliced scheduler

## The problem
A query engine has far more runnable pieces of work (**splits**) than threads. Running each split to completion on a thread means one huge split blocks every short one behind it. Trino's `TaskExecutor` instead runs each split for one short **quantum**, then puts it at the back of the queue so everyone gets a turn; a split that must wait for data gives its thread back instead of holding it. You build that scheduler.

## What you implement
`TimeSlicedExecutor(int threads, long quantumNanos)` in `src/main/java/dev/conc/p12/`:
- `CompletableFuture<Void> enqueue(SplitRunner split)`
- `void close()`

Given: `SplitRunner.process(long quantumNanos)` (do at most about that much work), `SplitResult` (`Finished`, `Yielded`, `Blocked(CompletableFuture<?> signal)`), and `BurnSplit(totalNanos)`, a split that burns CPU one slice at a time.

## Rules
- A **fixed** number of threads named `p12-split-0`, `p12-split-1`, ...; one **FIFO queue** of runnable splits.
- A worker takes the split at the head, calls `process(quantumNanos)` **once**, then:
  - `Finished`: complete the split's future normally;
  - `Yielded`: put the split at the **tail** of the queue (everyone else gets a turn);
  - `Blocked(signal)`: do **not** keep a thread on it. When the signal completes (normally or not), put the split at the tail of the queue;
  - exception from `process`: complete the split's future **exceptionally** with that exception and drop the split. Other splits are not affected.
- `enqueue(split)` returns a future that completes when the split is `Finished` (or failed).
- `close()` stops the threads (interrupt and join) and cancels the futures still pending. After `close()` no `p12-split-` thread is alive.

## Example
1 thread, quantum 10 ms. A long split `L = new BurnSplit(1 s)` is enqueued, then a short one `S = new BurnSplit(20 ms)`:
```
t=0    L runs 10 ms, Yielded   -> queue: S, L
t=10   S runs 10 ms, Yielded   -> queue: L, S
t=20   L runs 10 ms, Yielded   -> queue: S, L
t=30   S runs 10 ms, Finished  -> S's future completes at about 40 ms
...    L keeps going and finishes at about 1.02 s
```
Without slicing (huge quantum) S would wait for all of L: about 1 s.
A split that returns `Blocked(signal)` frees its thread at once: 10 000 splits mostly waiting need no more threads than 10 000 splits that are running.

## Run
`mvn -q test -Dtest='C05*'`

## Done when
All `C05*` tests pass and `../results/<date>-time-slicing-fairness.md` exists.

## Then, for the note (`../results/<date>-time-slicing-fairness.md`)
One long split and twenty short ones on one thread; measure the completion time of the short ones with a quantum of 1 ms, 10 ms, 100 ms and 10 s (effectively no slicing). Compare with giving each split its own thread.

## Think about
- Why does a quantum exist? What is the cost of a very small one (switching, queue contention) and of a very large one (latency of short work)?
- A split that returns `Blocked` gives its thread back. Where in Trino does that happen (waiting for an exchange buffer, for output buffer space)? What would happen to a one-thread-per-split design with 10,000 splits mostly waiting?
- This is cooperative scheduling: nothing preempts a split that ignores its quantum. What can a misbehaving split do, and how does Trino guard against it?
- Open `TaskExecutor`, `PrioritizedSplitRunner` and `Driver` in the Trino source. Note three differences from your version.
