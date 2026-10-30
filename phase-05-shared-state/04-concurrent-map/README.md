# 04 `ConcurrentHashMap` and `LongAdder`

**Do:** implement `ConcurrentProfileStore` without the `synchronized` keyword and without your own lock: `ConcurrentHashMap` per-key atomic operations for the profiles, `LongAdder` for the total.

**Run:** `mvn -q test -Dtest='C04*'`

**Trap to find yourself:** `ConcurrentHashMap` makes the *map* safe, not the object inside it. `UserBehaviourProfile` is still a plain mutable object. Where exactly can two threads touch the same profile, and which `ConcurrentHashMap` operation makes that one-at-a-time per key?

**Think about:**
- Why does `LongAdder` exist when `AtomicLong` does the same job? When is it faster, and when is it not? (Read the `LongAdder` / `Striped64` javadoc, then the source.)
- What does `LongAdder.sum()` promise while writers are running?
- Which of these three stores would you use in MiniTrino's memory accounting (V7), and why?
