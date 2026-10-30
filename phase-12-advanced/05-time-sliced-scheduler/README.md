# 05 Time-sliced scheduler

**Do:** implement `TimeSlicedExecutor`. `SplitRunner`, `SplitResult` and the CPU job `BurnSplit` are given. This is the model of Trino's `TaskExecutor` running `Driver`s.

**Run:** `mvn -q test -Dtest='C05*'`

**Then, for the note (`../results/<date>-time-slicing-fairness.md`):** one long split and twenty short ones on one thread; measure the completion time of the short ones with a quantum of 1 ms, 10 ms, 100 ms and 10 s (effectively no slicing). Compare with giving each split its own thread.

**Think about:**
- Why does a quantum exist? What is the cost of a very small one (switching, queue contention) and of a very large one (latency of short work)?
- A split that returns `Blocked` gives its thread back. Where in Trino does that happen (waiting for an exchange buffer, for output buffer space)? What would happen to a one-thread-per-split design with 10,000 splits mostly waiting?
- This is cooperative scheduling: nothing preempts a split that ignores its quantum. What can a misbehaving split do, and how does Trino guard against it?
- Open `TaskExecutor`, `PrioritizedSplitRunner` and `Driver` in the Trino source. Note three differences from your version.
