# 02 Event generator

## The problem
Later phases need a stream of fake user events to push through the engine. You build the source of that stream. It must be repeatable (so an experiment can be rerun and compared) and uneven (a few users are very busy, as in real traffic), because uneven load is what makes ordering problems and lock contention show up.

**Why this matters for concurrency:** this is your producer in Phases 2 to 4.

## What you implement
`EventGenerator` in `src/main/java/dev/conc/p1/`:
- `new EventGenerator(long seed, int users, int products, double skew)`
- `Event next()`: the next event of the stream.
- `List<Event> take(int n)`: the next `n` events (same as calling `next()` n times).

Users are named `user-0 .. user-(users-1)`, products `product-0 .. product-(products-1)`.

## Rules
1. **Repeatable.** Two generators with the same arguments produce streams where every field of every `Event` is equal, timestamps included. The seed is the only input: no wall clock, no unseeded randomness.
2. **`eventId`** is 1, 2, 3, ... across the whole stream.
3. **Timestamps** never go backwards.
4. **`userSeq`** is per user: each user's events are numbered 1, 2, 3, ... with no gaps, counting only that user's events.
5. **Who is the user of each event?** Each event's user is drawn independently. User `i` is chosen with probability `w_i / Σw`, where `w_i = 1 / (i + 1)^skew` and `Σw` is the sum of all `w_i`.
   - `skew = 0`: every `w_i` is 1, all users equally likely.
   - Example: 3 users, `skew = 1`: weights 1, 1/2, 1/3 (sum 11/6), so user-0 gets 6/11 of the events, user-1 3/11, user-2 2/11.
   - Example: 100 users, `skew = 1.2`: user-0 gets about 27.8% of the events.
6. **Types and products** can be random, but all four `EventType`s must appear in a long enough stream.
7. **Bad arguments** throw `IllegalArgumentException` (the tests show which).

The tests check rule 5 only loosely. The formula is the spec, not the tests.

## Example (shape, not exact values; yours depend on your random numbers)
With `new EventGenerator(42, 3, 5, 0)` and `take(5)` you might get:
```
eventId  userId  productId  type      timestamp  userSeq
1        user-2  product-4  VIEW      0          1
2        user-0  product-1  CLICK     3          1
3        user-2  product-0  VIEW      3          2     <- user-2's second event
4        user-1  product-1  PURCHASE  7          1
5        user-0  product-2  VIEW      9          2
```
Creating another generator with seed 42 and the same arguments gives these exact five rows again.

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass, and you can say why your user choice follows the formula in rule 5.

## Think about
The generator is *not* thread-safe. Why is that fine for now, and what will you have to decide in Phase 3 (multiple producers)?

## Hint
Build a cumulative weight array once, then binary-search a random number in it.
