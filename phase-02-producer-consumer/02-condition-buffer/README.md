# 02 Condition buffer

## The problem
Same buffer as challenge 1, but built with the tools `java.util.concurrent.locks` gives you, and with two extra operations that **give up after a deadline** instead of waiting forever. A real system needs those: a producer that cannot get space within 50 ms should shed load or report an error, not hang.

## What you implement
`ConditionBuffer<T>` in `src/main/java/dev/conc/p2/`, implementing `TimedBuffer<T>`:
- everything from challenge 1: constructor, `put`, `take`, `size`
- `boolean offer(T item, long timeout, TimeUnit unit)`: put, waiting up to the timeout for space
- `T poll(long timeout, TimeUnit unit)`: take, waiting up to the timeout for an item

Use one `ReentrantLock` and two `Condition`s (`notFull`, `notEmpty`).

## Rules
- Same contract as challenge 1 for `put`, `take`, `size`, capacity, `null`, FIFO, interruption.
- `offer` returns `true` if the item was added, `false` if no space appeared in time (the buffer is unchanged).
- `poll` returns the item, or `null` if none arrived in time.
- A timeout of `0` means "do not wait at all".
- The total wait is the timeout, **not** the timeout per wakeup: if you are woken early and the condition is still false, you wait only for the time that is left.
- `offer(null, ...)` throws `NullPointerException`.

## Example
Capacity 1:
```
offer(1, 0, MILLISECONDS)     -> true    (space was free, did not wait)
offer(2, 0, MILLISECONDS)     -> false   (full, did not wait)
poll(0, MILLISECONDS)         -> 1
poll(0, MILLISECONDS)         -> null    (empty, did not wait)

poll(120, MILLISECONDS)       -> null    after about 120 ms (nobody put anything)

// another thread does put(42) after 80 ms:
poll(3, SECONDS)              -> 42      after about 80 ms, not 3 s
```

## Run
`mvn -q test -Dtest='C02*'`. The first tests are the same contract as challenge 1; the timeout tests are new.

## Done when
All `C02*` tests pass.

## Think about
- With two conditions you can use `signal` and not `signalAll`. Why is that safe here, and what changed compared with the `synchronized` version?
- `awaitNanos` returns the time left. What goes wrong if you wait for the full timeout again after a spurious wakeup?
- Where must `unlock` be? What happens to the lock if `await` is interrupted?
