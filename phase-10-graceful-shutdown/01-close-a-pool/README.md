# 01 Close a pool

**Do:** implement `GracefulShutdown.close`. Three cases: it finishes in time, it does not, the caller is interrupted. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`

**Think about:**
- `shutdown()` and `shutdownNow()` do different things to the queue and to running tasks. What exactly does each one do to a task that is *running*? To one that is *waiting*?
- `shutdownNow()` only sends interrupts. What kind of task survives it, and how long can your `close` stay blocked on such a task?
- If the thread calling `awaitTermination` is interrupted, what do you do with that interrupt? Why must you set the flag again?
- Which tasks in `neverStarted` do you now need to do something with (re-queue, log, persist)? In Phase 8 terms, whose responsibility are they?
