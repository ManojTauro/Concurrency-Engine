# 03 Lane profile store

## The problem
In phase 5 the profiles were shared and protected with locks. Here you use the **single-writer** idea: each user belongs to one lane, and only that lane's thread ever touches the user's profile. Nothing is shared, so nothing needs a lock; the lane's queue provides the ordering. You build a profile store on top of your `LaneExecutor` and make it detect out-of-order events.

## What you implement
`LaneProfileStore(int lanes)` in `src/main/java/dev/conc/p6/`, implementing `OrderedStore` (`record` returns before the event is applied):
- `void record(Event event)`: hand the event to its user's lane
- `void awaitIdle()`: return when everything recorded before the call has been applied
- `long count(String userId, EventType type)`, `long score(String userId)`: `0` for an unknown user
- `long totalEvents()`, `long orderViolations()`: exact once idle
- `void close()`: shut the lanes down after queued work finishes

`UserBehaviourProfile` is given (plain, not thread-safe). Use your `LaneExecutor` from challenge 1.

## Rules
- Only a user's lane thread ever touches that user's profile. No lock and no concurrent map around the profiles.
- Before applying an event the lane compares `event.userSeq()` with `profile.lastUserSeq()`. If `userSeq` is **not larger**, count one violation (the event is still applied).
- `count` and `score` read **through the same lane** (submit the read, wait for the answer), so they never see a half-applied event.
- `record` after `close()` throws `RejectedExecutionException`.
- Works with one lane and with more lanes than users.

## Example
One user, events recorded in this order with these `userSeq` values: `1, 3, 2, 2`.
```
seq 1: 1 > 0           ok
seq 3: 3 > 1           ok
seq 2: 2 <= 3          violation
seq 2: 2 <= 3          violation
awaitIdle();
orderViolations() -> 2     totalEvents() -> 4
```
With events arriving in order (`1, 2, 3, ...`) per user, `orderViolations() == 0`, even with many threads calling `record` for different users.

## Run
`mvn -q test -Dtest='C03*'`. Then run `OrderingDemoMain` for the experiment (it compares the given naive pool store with your lanes).

## Done when
All `C03*` tests pass and `../results/<date>-ordering-violations.md` has both the naive and the lane rows.

## Think about
- Phase 5 protected shared profiles with locks. Here nothing is shared. Why is that safe, and what does the lane's queue give you in place of a lock (think happens-before)?
- A read goes through the lane too. What does that cost, and what do you gain compared with a lock-free read of a half-updated profile?
- `totalEvents()` is a `LongAdder` summed by another thread. What do you read during writes, and what once the store is idle?
- When is this design worse than Phase 5's `ConcurrentHashMap` store?
