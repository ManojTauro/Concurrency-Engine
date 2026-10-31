# 04 Idempotency

## The problem
Retries (challenge 1) mean the same event can be delivered twice. Applying it twice would double-count. The cure is to make processing **idempotent**: remember which event ids were already handled and skip repeats. The memory must be bounded, and "have I seen this id? if not, remember it" must be **one atomic step**, because many threads race on the same ids.

## What you implement
In `src/main/java/dev/conc/p9/`:
1. `DedupFilter(int capacity)`:
   - `boolean firstTime(long eventId)`: `true` if the id is not remembered (and it becomes remembered), `false` if it is
   - `void forget(long eventId)`: remove an id so `firstTime` is `true` for it again
   - `int size()`: number of ids remembered
2. `AtMostOnceRecorder(DedupFilter filter, Consumer<Event> sink)` with `boolean record(Event event)`.

## Rules
- `DedupFilter`: the check and the remembering are **one atomic step**: when many threads call `firstTime` with the same id at the same moment, **exactly one** gets `true`.
- At most `capacity` ids are remembered. When a new id would exceed that, the id remembered **first** is forgotten (a repeat hit does not refresh an id). `capacity < 1` throws `IllegalArgumentException`.
- `AtMostOnceRecorder.record(event)`: if the event id has been seen, do nothing and return `false`. Otherwise pass the event to the sink and return `true`.
- If the sink **throws**, the id must be **forgotten again** (so a later retry of the same event can succeed) and the exception propagates.

## Example
```java
var f = new DedupFilter(2);
f.firstTime(1);   // true
f.firstTime(1);   // false  (seen)
f.firstTime(2);   // true
f.firstTime(3);   // true   -> id 1 (remembered first) is forgotten, size() == 2
f.firstTime(1);   // true   (forgotten, so it counts as new again; this now forgets 2)
f.forget(3);      // size() == 1 ; firstTime(3) is true again

var r = new AtMostOnceRecorder(new DedupFilter(100), sink);
r.record(e1);     // true,  sink called
r.record(e1);     // false, sink not called again
r.record(e2);     // sink throws -> exception propagates, e2's id is forgotten
r.record(e2);     // true,  sink called again (the retry)
```
Eight threads recording the same 1 000 ids: each id reaches the sink exactly once.

## Run
`mvn -q test -Dtest='C04*'`. Eight threads race on the same ids.

## Done when
All `C04*` tests pass.

## Think about
- "Check if seen, then record" as two steps is a classic race. Which single operation makes it atomic? Where in your code is the atomic step?
- A bounded filter forgets old ids. What can go wrong after it forgets, and how do you choose the capacity? What would a real system do instead (a unique key in the database, an idempotency token)?
- The recorder forgets the id when the sink throws. What if the sink threw *after* it had partly done its work? Is "at most once" or "exactly once" even achievable here? Look up why exactly-once delivery is hard.
