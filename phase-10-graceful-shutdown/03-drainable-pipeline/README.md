# 03 Drainable pipeline

**Do:** implement `DrainablePipeline`. The state machine and the guarantees are in the Javadoc.

**Run:** `mvn -q test -Dtest='C03*'`. The race test runs 10 rounds of four submitters against a `close`.

**Think about:**
- "Check that we are not closed, then enqueue" is two steps. What can happen between them when another thread closes? How does your design make "accepted" and "will be processed" one atomic fact? (A read-write lock, a counter of in-flight submits, and a state machine are three answers: compare them.)
- A submitter is blocked on a full queue when `close` starts. Does it get in or get refused? Which of the two keeps the guarantee?
- How do the workers learn that no more events are coming? Compare poison pills with "queue empty and closing".
- `Runtime.addShutdownHook` threads run when the JVM gets SIGTERM, concurrently with each other, with no ordering. What can go wrong with a hook that takes 60 seconds when the orchestrator waits 30 before SIGKILL?
