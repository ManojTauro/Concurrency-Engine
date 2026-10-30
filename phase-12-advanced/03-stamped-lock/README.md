# 03 Stamped lock

**Do:** implement `ScoreBoard` with a `StampedLock`: write lock for `set`, an optimistic read with a validated stamp for `read`, and the read lock as the fallback.

**Run:** `mvn -q test -Dtest='C03*'`. One test reads from six threads while a writer runs and checks that no snapshot is ever torn.

**Think about:**
- In an optimistic read you copy the fields *before* you validate. Why must you never use the values (or follow pointers from them) before `validate` says yes?
- Compare against `ReentrantReadWriteLock` and `synchronized` with 8 readers and 1 writer, then with 8 readers and no writer. Where does `StampedLock` win, by how much, and why (readers do not write to shared memory)?
- `StampedLock` is not reentrant and has no `Condition`. What bugs does that invite?
- When is the optimistic path a bad idea (write-heavy workloads, long reads)?
