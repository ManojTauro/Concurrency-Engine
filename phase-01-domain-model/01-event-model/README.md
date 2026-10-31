# 01 Event model

## The problem
The engine passes user actions around as `Event` objects: "user-7 viewed product-3". One thread creates an event, many threads read it. You make `Event` safe to share and make it impossible to build a nonsense event (no user, negative timestamp).

**Why this matters for concurrency:** if an object is immutable (all fields final, no setters, nothing mutable inside), any thread can read it with no lock and no visibility problem. That is the cheapest kind of thread safety there is.

## What you implement
In `src/main/java/dev/conc/p1/`:
- `EventType.weight()`: VIEW 1, CLICK 2, ADD_TO_CART 5, PURCHASE 10.
- The `Event` record's compact constructor (the validation).

## Rules
| Field | Invalid when | Throws |
|---|---|---|
| `userId`, `productId`, `type` | `null` | `NullPointerException` |
| `userId`, `productId` | blank (`""`, `"   "`) | `IllegalArgumentException` |
| `eventId` | less than 1 | `IllegalArgumentException` |
| `userSeq` | less than 1 | `IllegalArgumentException` |
| `timestampMillis` | less than 0 (0 is allowed) | `IllegalArgumentException` |

A `null` is checked before blankness: `new Event(1, null, ...)` is an NPE, not an IAE.

## Examples
```java
new Event(1, "user-7", "product-3", EventType.VIEW, 0, 1);       // ok (timestamp 0 is fine)
new Event(1, null,     "product-3", EventType.VIEW, 0, 1);       // NullPointerException
new Event(1, "  ",     "product-3", EventType.VIEW, 0, 1);       // IllegalArgumentException
new Event(0, "user-7", "product-3", EventType.VIEW, 0, 1);       // IllegalArgumentException (eventId < 1)
new Event(1, "user-7", "product-3", EventType.VIEW, -5, 1);      // IllegalArgumentException (timestamp < 0)
EventType.PURCHASE.weight();                                     // 10
```

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass.

## Think about
Why is a record with only final primitives, strings and an enum safe to share between threads? What would break if `Event` held a `List<String>` field?
