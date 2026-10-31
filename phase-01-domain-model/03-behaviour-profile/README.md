# 03 Behaviour profile

## The problem
For every user the engine keeps a summary of what they did: how many views, clicks, carts and purchases; an interest score; which products they touched. That summary is a `UserBehaviourProfile`. Each incoming event is folded into it with `apply`. Write it for **one thread only**: plain fields, no locks, no atomics.

**Why this matters for concurrency:** this is the shared mutable state Phase 5 races on. Keeping it plain is what makes the race visible later.

## What you implement
`UserBehaviourProfile` in `src/main/java/dev/conc/p1/`:

| Method | Returns |
|---|---|
| `new UserBehaviourProfile(userId)` | `null` is an NPE, blank is an IAE |
| `userId()` | the id it was created with |
| `apply(Event e)` | folds the event in (see rules) |
| `count(EventType t)` | how many events of that type were applied |
| `score()` | sum of `type.weight()` over all applied events |
| `distinctProducts()` | number of different `productId`s seen |
| `lastUserSeq()` | highest `userSeq` applied so far, `0` if none |
| `lastTimestampMillis()` | largest timestamp applied so far, `-1` if none |

## Rules
- `apply(null)` throws `NullPointerException`.
- An event whose `userId` differs from the profile's throws `IllegalArgumentException` and changes nothing.
- `lastUserSeq` and `lastTimestampMillis` are the **largest seen**, not the last applied. Events can arrive out of order later.

## Example
```java
var p = new UserBehaviourProfile("u");
p.apply(new Event(1, "u", "a", EventType.VIEW,     10, 1));
p.apply(new Event(2, "u", "a", EventType.VIEW,     11, 2));
p.apply(new Event(3, "u", "b", EventType.PURCHASE, 13, 3));
p.count(EventType.VIEW);      // 2
p.count(EventType.CLICK);     // 0
p.score();                    // 1 + 1 + 10 = 12
p.distinctProducts();         // 2  ("a" and "b")
p.lastUserSeq();              // 3
p.lastTimestampMillis();      // 13

// out of order: seq 5 arrives, then seq 3
p.apply(new Event(9, "u", "a", EventType.VIEW, 50, 5));
p.apply(new Event(8, "u", "a", EventType.VIEW, 30, 3));
p.lastUserSeq();              // 5, not 3
p.lastTimestampMillis();      // 50, not 30

new UserBehaviourProfile("u").lastTimestampMillis();   // -1 (nothing applied yet)
```

## Run
`mvn -q test -Dtest='C03*'`

## Done when
All `C03*` tests pass.

## Think about
`apply` reads and writes several fields. Which pairs of fields must change together for the profile to make sense? (That group is what a lock would have to protect.)
