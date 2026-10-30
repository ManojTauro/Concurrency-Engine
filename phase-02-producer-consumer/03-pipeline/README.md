# 03 Pipeline

**Do:** implement `EventPipeline.run` in `src/main/java/dev/conc/p2/`. One producer thread, one consumer thread, an `ArrayBlockingQueue`. The contract is in the Javadoc above `run`.

**Run:** `mvn -q test -Dtest='C03*'`

**Think about:**
- How does the consumer learn that the producer is finished? Name two ways (a poison pill, a shared flag) and say what each one risks.
- The sink throws on the tenth event while the producer is blocked on a full queue. Who unblocks the producer?
- Which thread's exception do you report if both threads fail?
- What does `InterruptedException` mean in each thread, and what do you do with the interrupt flag?
