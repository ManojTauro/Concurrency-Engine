# 06 Multilevel queue

## The problem
A plain FIFO queue treats a split that has used one millisecond the same as one that has used ten minutes. Trino's `MultilevelSplitQueue` puts work that has used **little** CPU time at a high-priority level (probably a short query) and work that has used a lot at lower levels, so short work finishes fast. Strict priority would starve long work, so each level is also entitled to a share of the total time. You build a simplified version, as pure data structure logic (no threads).

## What you implement
`MultilevelQueue<T>(long[] thresholdsNanos)` in `src/main/java/dev/conc/p12/`. `LevelEntry<T>` is given (it holds the item, `scheduledNanos()` and `level()`).
- `LevelEntry<T> add(T item)`
- `LevelEntry<T> poll()`
- `void charge(LevelEntry<T> entry, long nanos)`
- `void requeue(LevelEntry<T> entry)`
- `long consumed(int level)`

## Rules
- One FIFO queue per level. `thresholds[i]` is the scheduled time at which an entry moves up to level `i`. `thresholds[0]` must be `0` and the array strictly ascending, otherwise `IllegalArgumentException`. Not thread-safe: the caller synchronises.
- `add(item)`: a new entry with `scheduledNanos` 0, put at the **tail of level 0**, and returned.
- `poll()`: remove and return an entry (`null` if the queue is empty). Among the **non-empty** levels choose the level `i` with the **smallest `consumed(i) * 2^i`** (ties: the lower level) and take the **head** of that level's FIFO. Level `i` is entitled to half the time of level `i-1`.
- `charge(entry, nanos)`: the entry used `nanos` of CPU. Add `nanos` to `consumed(entry.level())` for the level it was at **before** the charge, then add `nanos` to the entry's own `scheduledNanos`.
- `requeue(entry)`: put the entry at the tail of the level its `scheduledNanos` now gives it.
- `consumed(level)`: total nanos charged to entries while they were at that level.

## Example
Thresholds `{0, 100, 1000}` (read as ms), entries X and Y added (both at level 0):
```
poll()               -> X                 (only level 0 is non-empty)
charge(X, 150)       consumed(0) = 150, X.scheduled = 150 -> X.level() is now 1
requeue(X)           X goes to level 1.   level 0: [Y]   level 1: [X]
poll()               level 0: 150 * 1 = 150     level 1: 0 * 2 = 0     -> level 1 wins -> X
charge(X, 50)        X was at level 1 BEFORE the charge: consumed(1) = 50, X.scheduled = 200
requeue(X)
poll()               level 0: 150     level 1: 50 * 2 = 100    -> level 1 again -> X
charge(X, 50)        consumed(1) = 100
requeue(X)
poll()               level 0: 150     level 1: 100 * 2 = 200   -> level 0 wins -> Y
```
So the long entry X is served more often at first, but level 0 gets its share and Y is not starved.
`new MultilevelQueue<>(new long[]{5, 10})` throws (first threshold is not 0). `poll()` on an empty queue returns `null`.

## Run
`mvn -q test -Dtest='C06*'`

## Done when
All `C06*` tests pass.

## Then, as an extension (not tested)
Replace the plain FIFO queue in your `TimeSlicedExecutor` with a `MultilevelQueue` (guard it with a lock), charge each slice's real elapsed time, and repeat the fairness experiment from challenge 5 with a long split that never finishes: do short splits that arrive later still get through quickly?

## Think about
- Splits that have used little CPU time are probably short queries; ones that have used a lot are probably big ones. Why is serving the "young" first good for latency overall (think shortest-job-first)?
- Strict priority would starve the long splits. How does the weighted share between levels prevent that, and what does Trino use for the weights? (Read `MultilevelSplitQueue` for its target shares.)
- Why charge the level the entry was at *before* the charge?
