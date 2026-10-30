# 03 Behaviour profile

**Why this matters for concurrency:** `UserBehaviourProfile` is the shared mutable state Phase 5 races on. Write it plainly and correctly for one thread, with no locks and no atomics. Keeping it plain is what makes the race visible later.

**Do:** implement `UserBehaviourProfile` in `src/main/java/dev/conc/p1/`:
- `apply(event)` folds an event in: per-type counts, score (sum of weights), the set of distinct products, the highest `userSeq`, the largest timestamp.
- An event for another user throws `IllegalArgumentException`; `null` throws `NullPointerException`.
- `lastUserSeq` is the highest seen, not the last applied. Events can arrive out of order later.

**Run:** `mvn -q test -Dtest='C03*'`

**Think about:** `apply` reads and writes several fields. Which pairs of fields must change together for the profile to make sense? (That group is what a lock would have to protect.)
