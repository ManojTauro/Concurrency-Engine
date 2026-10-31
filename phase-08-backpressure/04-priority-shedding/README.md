# 04 Priority shedding

## The problem
When overloaded you cannot keep everything, so lose the **least valuable** work first. Here the value of an event is its type weight: `VIEW` 1 < `CLICK` 2 < `ADD_TO_CART` 5 < `PURCHASE` 10. A full queue should throw away a queued view to make room for an arriving purchase, but never the other way round.

## What you implement
`PriorityShedder(int capacity)` in `src/main/java/dev/conc/p8/`:
- `ShedOutcome offer(Event event)`
- `Event poll()`
- `int size()`
- `Map<EventType, Long> shedByType()`

`ShedOutcome` is `ACCEPTED`, `ACCEPTED_EVICTED` or `REJECTED`.

## Rules
- `offer`: if there is room, `ACCEPTED`. When **full**: find the queued event with the **lowest** weight (among equals, the one that arrived first). If the new event's weight is **strictly higher**, evict that one and return `ACCEPTED_EVICTED`. Otherwise return `REJECTED` (the new event is lost).
- `poll`: returns the queued event with the **highest** weight, equal weights in arrival order. `null` if empty.
- `shedByType()`: for every `EventType` (all four keys present, starting at 0), how many events of that type were **lost** (evicted or rejected).
- The queue never holds more than `capacity` events. All of this is safe from many threads.
- `capacity < 1` throws `IllegalArgumentException`.

## Example
Capacity 3, events `e1(VIEW) e2(VIEW) e3(VIEW)` queued:
```
offer(e4 VIEW)      -> REJECTED   (equal priority is not enough)    shedByType: VIEW=1
offer(e4 PURCHASE)  -> ACCEPTED_EVICTED   (evicts e1, the oldest lowest)   shedByType: VIEW=1
poll() -> e4, then e2, then e3     (highest weight first, then arrival order)

Full of [PURCHASE, PURCHASE, PURCHASE]:
offer(CLICK)        -> REJECTED   shedByType: CLICK=1, PURCHASE=0

Capacity 10, offered in this order: v1 (VIEW), c2 (CLICK), p3 (PURCHASE), c4 (CLICK), v5 (VIEW)
poll() gives: p3, c2, c4, v1, v5
```

## Run
`mvn -q test -Dtest='C04*'`. One test checks the policy after every single offer on a random stream; the others check order and the capacity under contention.

## Done when
All `C04*` tests pass.

## Think about
- Which operations need to be atomic together: "find the lowest", "compare", "evict", "insert"? What data structure makes the lowest and the highest both cheap?
- Shedding by type is a business decision. Who decides that a `VIEW` is worth less than a `PURCHASE`, and what if that is wrong for a flash sale?
- You shed work you already accepted. Is the caller told? What would a "429 Too Many Requests" with a `Retry-After` header do for the whole system (Phase 9)?
