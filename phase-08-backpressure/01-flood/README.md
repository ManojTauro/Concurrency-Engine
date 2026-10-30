# 01 Flood

**Do:** implement `FloodExperiment.run`. An unbounded `LinkedBlockingQueue`, a producer thread and a consumer thread at set rates, and a sampler. Contract in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`

**Think about (answer in `../results/<date>-flood.md`):**
- At 20,000 in and 1,000 out per second, how deep is the queue after 10 seconds? After a minute? How much memory is that?
- The consumer is never idle and never fails. So why is this a failure of the system? What happens to the *age* of the oldest queued event, and why does that matter more than the depth?
- Where does the first symptom show up in a real service (latency, GC, an `OutOfMemoryError`)? Which of them would an alert catch first?
