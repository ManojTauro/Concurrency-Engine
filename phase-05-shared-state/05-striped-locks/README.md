# 05 Striped locks with `ReentrantLock`

## The problem
One lock for all users is simple but serial. One lock per user is fast but costs memory and needs a lock map. The middle way is **striping**: split the users into N groups (stripes) by hashing the user id; each stripe has its own lock, so users in different stripes never wait on each other. You build it and measure.

## What you implement
`LockedProfileStore(int stripes)` in `src/main/java/dev/conc/p5/`, implementing `ProfileStore` (same four methods and meaning as challenge 3).

## Rules
- `stripes <= 0` throws `IllegalArgumentException` (constructor).
- The user ids are spread over `stripes` separate maps, each guarded by its own `ReentrantLock`.
- Always unlock in `finally`.
- Reads (`count`, `score`) take the right stripe's lock too.
- `totalEvents()` must be correct **without** taking every lock.
- Same contract as challenge 3, and it must pass with 1, 16 and 256 stripes.

## Example
`new LockedProfileStore(16)`: users `user-3` and `user-9` may land in the same stripe or in different ones (decided by the user id's hash); two threads recording for users in different stripes do not block each other, two threads recording for users in the same stripe do. With `stripes = 1` it behaves like challenge 3.

## Run
`mvn -q test -Dtest='C05*'`. It is tested with 1, 16 and 256 stripes.

## Done when
All `C05*` tests pass and the timing section of `../results/<date>-store-tradeoffs.md` is written.

## Measure (write `../results/<date>-store-tradeoffs.md`)
```
mvn -q compile
java -cp target/classes dev.conc.p5.ContentionMain
```
`ContentionMain` is a deliberately naive timing harness. Run it a few times and note how much the numbers move. Then write down your belief: which store is fastest, and why? You will redo this with JMH in Phase 11 and compare. Do not post these numbers as a conclusion.

## Think about
- What is the cost of a stripe? What if two hot users land in the same stripe? (The generator's skew makes that real.)
- Why is `totalEvents()` a `LongAdder` here and not a sum over the stripes under every lock?
- When would a plain `synchronized` store be the better choice?
