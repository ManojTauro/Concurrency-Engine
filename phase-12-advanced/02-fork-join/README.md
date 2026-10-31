# 02 Fork/join

## The problem
Summing the event weights per user over millions of events is CPU-bound and splits naturally: sum the left half, sum the right half, merge the two maps. **Fork/join** is built for this: a pool whose threads split a task into smaller tasks, and idle threads **steal** work from busy ones. You write it as a `RecursiveTask` and control the size below which you stop splitting.

## What you implement
`ParallelScorer.scoreByUser(List<Event> events, ForkJoinPool pool, int threshold)` returning `Map<String, Long>` (user id to sum of `type.weight()`), in `src/main/java/dev/conc/p12/`.

## Rules
- Use a `RecursiveTask` over a range of the list. If the range has at most `threshold` events, compute it directly into a `HashMap`. Otherwise split it in half, **fork one half, compute the other in the current thread**, join, and merge the two maps.
- The work must run **in the given pool** (`pool.invoke`), not the common pool, and must **not** use parallel streams.
- An empty list gives an empty map. `threshold < 1` throws `IllegalArgumentException`.

## Example
Events: `(alice, VIEW)`, `(alice, PURCHASE)`, `(bob, CLICK)`, `(alice, CLICK)`:
```
scoreByUser(events, pool, 2)  ->  {alice=13, bob=2}      (1 + 10 + 2, and 2)
    range [0,4) > threshold 2  -> split into [0,2) and [2,4)
    [0,2) -> {alice=11}        [2,4) -> {bob=2, alice=2}      merged: {alice=13, bob=2}
scoreByUser(List.of(), pool, 2) -> {}
scoreByUser(events, pool, 0)    -> IllegalArgumentException
```
The result must be identical to a plain sequential loop, for any threshold.

## Run
`mvn -q test -Dtest='C02*'`. The test checks that your work ran in the pool it was given.

## Done when
All `C02*` tests pass.

## Think about
- Why fork one half and compute the other half yourself, instead of forking both?
- What does the threshold control? Try 1, 100, 10,000 and 1,000,000 on 5 million events and see the time. Too small: task overhead. Too big: idle cores.
- Fork/join suits CPU-bound, divide-and-conquer work that does not block. What goes wrong if a task blocks on I/O inside a `ForkJoinPool`? (And why do parallel streams share the common pool with everyone else?)
- Work stealing: which end of the deque does the owner use, and which end does a thief use? Why?
