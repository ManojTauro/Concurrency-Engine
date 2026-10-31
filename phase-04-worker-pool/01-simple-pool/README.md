# 01 Simple pool

## The problem
Starting a new thread per task is expensive and unbounded. A **worker pool** starts a fixed number of threads once; they pull tasks from a queue and run them. You build the smallest correct version: threads, a bounded queue, and a clean shutdown. No `ExecutorService`.

## What you implement
`SimplePool` in `src/main/java/dev/conc/p4/`:
- `new SimplePool(int workers, int queueCapacity)`: starts the workers in the constructor
- `void submit(Runnable task)`
- `void shutdown()`
- `boolean awaitTermination(long timeout, TimeUnit unit)`

You may use a `BlockingQueue`.

## Rules
- Worker threads are named `p4-simple-0`, `p4-simple-1`, ... Never more than `workers` threads.
- `submit` queues the task. If the queue is full, or the pool is shut down, it throws `RejectedExecutionException` and **the task never runs**.
- A task that throws must not kill its worker; the worker goes on to the next task.
- `shutdown()` stops accepting new tasks. Tasks already queued **still run**. It does not wait. Calling it twice is harmless.
- `awaitTermination` returns `true` once every task has run and every worker thread has ended, and `false` if the time ran out first.
- After termination no `p4-simple-*` thread is alive.

## Example
`new SimplePool(1, 2)`; the one worker is busy with a task that blocks on a latch:
```
submit(A)   // queued (worker busy)
submit(B)   // queued, queue now full (2 items)
submit(C)   // throws RejectedExecutionException; C never runs
latch released: worker runs the blocker, then A, then B
shutdown(); awaitTermination(10, SECONDS) -> true
```
After `shutdown()`, `submit(x)` throws `RejectedExecutionException`.
`awaitTermination(100, MILLISECONDS)` while a 600 ms task is running returns `false` after about 100 ms.

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass.

## Think about
- How does an idle worker wake up for new work, and how does it learn the pool is shutting down? Compare polling with a timeout and a poison pill.
- A task throws. What happens to the thread running it if you do nothing? Where does the exception go?
- `submit` and `shutdown` race. Which tasks may still be accepted after `shutdown` has returned, and how do you make that exact?
