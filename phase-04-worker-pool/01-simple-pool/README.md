# 01 Simple pool

**Do:** implement `SimplePool` with plain threads and a `BlockingQueue`. No `ExecutorService`. The rules are in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`

**Think about:**
- How does an idle worker wake up for new work, and how does it learn the pool is shutting down? Compare polling with a timeout and a poison pill.
- A task throws. What happens to the thread running it if you do nothing? Where does the exception go?
- `submit` and `shutdown` race. Which tasks may still be accepted after `shutdown` has returned, and how do you make that exact?
