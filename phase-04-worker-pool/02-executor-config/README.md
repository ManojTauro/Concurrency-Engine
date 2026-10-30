# 02 Executor config

**Do:** implement `PoolFactory.create(PoolSpec)` so the tests on growth order, rejection and expiry pass.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about (this is the famous surprise):**
- With core 2, max 4 and a queue of 2, the third task does NOT create a third thread. Why? When does the pool grow beyond the core size?
- What does that mean for an unbounded queue and `max`? Why is `max` then useless?
- `CallerRunsPolicy` slows the submitter down. Why is that sometimes exactly what you want (hint: Phase 8)?
- What does `DiscardOldest` lose, and when would you accept that?
