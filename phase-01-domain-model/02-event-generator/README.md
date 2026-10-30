# 02 Event generator

**Why this matters for concurrency:** this is your producer in Phases 2 to 4, and the load it makes decides which problems you hit. Uneven load (a few hot users) is what makes per-user ordering and lock contention show up. Determinism (same seed, same stream) is what lets you rerun an experiment and compare.

**Do:** implement `EventGenerator` in `src/main/java/dev/conc/p1/`:
- Same seed gives the same stream.
- `eventId` runs 1, 2, 3 ... across the stream. Timestamps never go backwards.
- Each user's `userSeq` counts 1, 2, 3 ... with no gaps, counting only that user's events.
- User choice: weight of user `i` is `1 / (i + 1)^skew`. With `skew = 0` all users are equally likely.
- Event types and products can be random, but all four types must appear.
- Bad arguments throw `IllegalArgumentException`.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:** the generator is *not* thread-safe. Why is that fine for now, and what will you have to decide in Phase 3 (multiple producers)?

**Hint:** build a cumulative weight array once, then binary-search a random number in it.
