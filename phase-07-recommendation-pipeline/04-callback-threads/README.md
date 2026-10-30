# 04 Callback threads

**Do:** implement `RankStage` so the heavy step runs on the given `cpuPool`.

**Run:** `mvn -q test -Dtest='C04*'`. One test starts a 30 ms timer while eight 25 ms CPU-heavy steps are running.

**Think about:**
- A non-async callback (`thenApply`) runs on whichever thread completes the future, or on the caller if it is already complete. In a real client library that thread is often a single I/O thread. What happens to every other request when you do 25 ms of CPU work on it?
- `thenApplyAsync` without an executor uses `ForkJoinPool.commonPool()`. Why is that also a poor choice for blocking or heavy work?
- Which pools in a real service should be separate: the I/O threads, the CPU work, the blocking calls? What does each need to be sized for? (Phase 4 and 11)
