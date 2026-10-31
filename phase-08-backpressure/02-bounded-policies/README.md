# 02 Bounded policies

## The problem
An unbounded queue just hides overload. A **bounded** queue forces a decision when it is full: make the producer wait, refuse the new event, throw away the oldest, or wait a limited time and then refuse. You build one queue that supports all four, selectable by a policy, and make it correct under many threads.

## What you implement
`BoundedIngest(int capacity, Policy policy, Duration timeout)` in `src/main/java/dev/conc/p8/`:
- `Outcome submit(Event event) throws InterruptedException`
- `Event take() throws InterruptedException`: the oldest event, waiting if there is none
- `int size()`, `long rejected()`, `long droppedOldest()`

`Outcome` is `ACCEPTED`, `ACCEPTED_DROPPED_OLDEST` or `REJECTED`.

## Rules
FIFO. The queue **never holds more than `capacity`** events, whatever the threads do. `capacity < 1` throws `IllegalArgumentException`. When there is room, every policy returns `ACCEPTED`.

When it is **full**:
| Policy | `submit` does | Counter |
|---|---|---|
| `BLOCK` | waits for space (interruptible), then returns `ACCEPTED` | none |
| `REJECT_NEW` | returns `REJECTED` at once | `rejected()` +1 |
| `DROP_OLDEST` | removes the oldest event, adds the new one, returns `ACCEPTED_DROPPED_OLDEST`; never waits | `droppedOldest()` +1 |
| `TIMEOUT` | waits up to `timeout` for space; if none appears returns `REJECTED` | `rejected()` +1 |

## Example
Capacity 2, events with ids 1..5, nobody calls `take()`:
```
REJECT_NEW : submit(1)=ACCEPTED submit(2)=ACCEPTED submit(3)=REJECTED submit(4)=REJECTED
             rejected()=2   take() gives 1, then 2
DROP_OLDEST: submit(1) submit(2) -> ACCEPTED, ACCEPTED
             submit(3)=ACCEPTED_DROPPED_OLDEST  (1 is gone: queue is 2,3)
             submit(4)=ACCEPTED_DROPPED_OLDEST  (queue is 3,4)
             droppedOldest()=2   take() gives 3, then 4
BLOCK      : submit(3) waits; another thread calls take() -> 1; submit(3) now returns ACCEPTED
TIMEOUT    : timeout 150 ms: submit(3) returns REJECTED after about 150 ms (or ACCEPTED if a take() freed space meanwhile)
```
Under 8 producers hammering a capacity-50 `REJECT_NEW` queue, accepted + rejected equals the number submitted, and `size()` never exceeds 50.

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about
- `BLOCK` pushes the slowness back to the producer. Who is the producer in a real system, and what can it do if it is blocked (an HTTP handler thread, a Kafka consumer)?
- `REJECT_NEW` and `TIMEOUT` hand the problem to the caller. What should the caller do with a rejected event?
- `DROP_OLDEST` favours fresh data. Which events is it right for (a live dashboard) and wrong for (a payment)?
- The capacity must hold under contention. How do you make "drop oldest, then add" safe when many threads do it at once?
