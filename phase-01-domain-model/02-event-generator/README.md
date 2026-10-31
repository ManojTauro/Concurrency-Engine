# 02 Event generator

**Why this matters for concurrency:** this is your producer in Phases 2 to 4, and the load it makes decides which problems you hit. Uneven load (a few hot users) is what makes per-user ordering and lock contention show up. Determinism (same seed, same stream) is what lets you rerun an experiment and compare.

**Do:** implement `EventGenerator` in `src/main/java/dev/conc/p1/`:
- Same seed gives the same stream: every field of every `Event` is equal, timestamps included. The seed must be the only input, so no wall clock and no unseeded randomness.
- `eventId` runs 1, 2, 3 ... across the stream. Timestamps never go backwards.
- Each user's `userSeq` counts 1, 2, 3 ... with no gaps, counting only that user's events.
- User choice: each event's user is drawn independently, and user `i` is chosen with probability `w_i / Σw`, where `w_i = 1 / (i + 1)^skew` and `Σw` is the sum of all users' weights. With `skew = 0` all users are equally likely. Example: 3 users, `skew = 1` gives weights 1, 1/2, 1/3, so shares of 6/11, 3/11 and 2/11.
- The tests check this only loosely (even at `skew = 0`; user-0 above 10% and busiest at `skew = 1.2`, where the formula predicts about 27.8% for 100 users). The formula above is the spec, not the tests.
- Event types and products can be random, but all four types must appear.
- Bad arguments throw `IllegalArgumentException`.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:** the generator is *not* thread-safe. Why is that fine for now, and what will you have to decide in Phase 3 (multiple producers)?

**Hint:** build a cumulative weight array once, then binary-search a random number in it.
