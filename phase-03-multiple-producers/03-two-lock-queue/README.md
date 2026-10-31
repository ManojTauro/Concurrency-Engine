# 03 Two-lock queue

## The problem
`ArrayBlockingQueue` uses one lock, so a producer and a consumer block each other even when they touch different ends of the queue. Michael and Scott (1996) showed a linked queue where **producers only contend with producers (tail lock) and consumers only with consumers (head lock)**. You build it, then measure whether it helps.

## What you implement
`TwoLockQueue<T>` in `src/main/java/dev/conc/p3/`: an unbounded FIFO singly linked list with a head lock and a tail lock.
- `void offer(T item)`
- `T poll()`
- `int size()`

## Rules
- `offer(null)` throws `NullPointerException`.
- `poll()` returns `null` when the queue is empty. It **never waits**.
- FIFO, and the items of any one producer come out in the order that producer offered them.
- Each item is polled exactly once, with many producers and consumers at the same time.
- `size()` is exact once all operations have finished, and never negative while operations are running.
- An `offer` and a `poll` must be able to run at the same time even on a queue with 0 or 1 items (that is what the dummy first node is for: decide what it is).

## Example
```
q = new TwoLockQueue<Integer>()
q.poll()      -> null         q.size() -> 0
q.offer(1); q.offer(2); q.offer(3)
q.size()      -> 3
q.poll()      -> 1
q.poll()      -> 2
q.poll()      -> 3
q.poll()      -> null         (empty again, and the queue still works)
q.offer(9); q.poll() -> 9     (reuse after becoming empty)
```
Four producers each offering 0..49 999 (tagged by producer) while four consumers poll: every one of the 200 000 items is seen exactly once, and each producer's items arrive in increasing order.

## Run
`mvn -q test -Dtest='C03*'`. The tests run several producers and consumers, check each producer's order, and watch `size()` from another thread.

## Done when
All `C03*` tests pass, and you have written `../results/<date>-queue-contention.md` (the timing comparison below).

## Think about (answer in `../results/<date>-queue-contention.md`)
- Time your `TwoLockQueue` against `LinkedBlockingQueue` and `ArrayBlockingQueue` with 1, 2, 4 and 8 producers and consumers. What do you see and why?
- Why does the list start with a dummy node? What would an `offer` and a `poll` on a one-item queue fight over without it?
- The `next` field of a node is written under the tail lock and read under the head lock. What guarantees the consumer sees it?
- Where do you update the size counter, and why does the order of "count" and "link" matter for a reader on another thread?
- How does `LinkedBlockingQueue` in the JDK do it? Read its source and note one difference.
