# 03 Lane profile store

**Do:** implement `LaneProfileStore` on top of your `LaneExecutor`. The profile objects are plain, non-thread-safe `UserBehaviourProfile`s and nothing locks them: only one lane thread ever touches a given profile. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C03*'`. Then run `OrderingDemoMain` for the experiment.

**Think about:**
- Phase 5 protected shared profiles with locks. Here nothing is shared. Why is that safe, and what does the lane's queue give you in place of a lock (think happens-before)?
- A read goes through the lane too. What does that cost, and what do you gain compared with a lock-free read of a half-updated profile?
- `totalEvents()` is a `LongAdder` summed by another thread. What do you read during writes, and what once the store is idle?
- When is this design worse than Phase 5's `ConcurrentHashMap` store?
