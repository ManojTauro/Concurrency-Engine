# 04 `ConcurrentHashMap` and `LongAdder`

## The problem
One lock around everything (challenge 3) means every thread waits for every other. The JDK has a map built for concurrent use and a counter built for heavy contention. You make the same store correct using those, with no lock of your own.

## What you implement
`ConcurrentProfileStore` in `src/main/java/dev/conc/p5/`, implementing `ProfileStore` (same four methods and meaning as challenge 3: `record`, `count`, `score`, `totalEvents`).

## Rules
- No `synchronized` keyword and no lock of your own.
- Use `ConcurrentHashMap` per-key atomic operations for the profiles, and `LongAdder` (or `AtomicLong`) for the total.
- Same contract as challenge 3: exact counts and scores after writers finish, `totalEvents()` never goes backwards, unknown user gives `0`, readers never fail.

## Example
Same as challenge 3: 8 threads recording 40 000 events for 64 users, then every count, score and the total match exactly. A store that only makes the *map* safe will still lose counts inside a user's profile.

## Run
`mvn -q test -Dtest='C04*'`

## Done when
All `C04*` tests pass.

## Trap to find yourself
`ConcurrentHashMap` makes the *map* safe, not the object inside it. `UserBehaviourProfile` is still a plain mutable object. Where exactly can two threads touch the same profile, and which `ConcurrentHashMap` operation makes that one-at-a-time per key?

## Think about
- Why does `LongAdder` exist when `AtomicLong` does the same job? When is it faster, and when is it not? (Read the `LongAdder` / `Striped64` javadoc, then the source.)
- What does `LongAdder.sum()` promise while writers are running?
- Which of these three stores would you use in MiniTrino's memory accounting (V7), and why?
