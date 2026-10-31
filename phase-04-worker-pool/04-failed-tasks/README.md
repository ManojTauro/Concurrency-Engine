# 04 Failed tasks

## The problem
A task that throws inside a thread pool can **vanish silently**. With `execute(Runnable)` the exception reaches the thread's uncaught-exception handler (often just a log line). With `submit(...)` it is captured inside a `Future`, and if nobody calls `get()` the failure is never seen. You make a pool that reports every failure exactly once, whichever way the task was started.

## What you implement
`FailureAwarePool` in `src/main/java/dev/conc/p4/` (a `ThreadPoolExecutor`; the constructor is given and takes a `Consumer<Throwable> onFailure`). Fill in:
- `afterExecute(Runnable r, Throwable t)`
- `long failureCount()`

## Rules
- Every task that **failed**, started with `execute(Runnable)` or `submit(...)`, is reported to `onFailure` **exactly once** with its **original** exception (not an `ExecutionException` wrapping it).
- A successful task reports nothing. A **cancelled** task is not a failure.
- `failureCount()` is the number of failures reported.
- `submit(...)` still returns a `Future` whose `get()` throws `ExecutionException` as usual.
- If `onFailure` itself throws, the pool keeps working.

## Example
```java
var pool = new FailureAwarePool(2, t -> log(t));
pool.execute(() -> { throw new IllegalStateException("x"); });
   // onFailure gets that exact IllegalStateException; failureCount() == 1
Future<?> f = pool.submit(() -> { throw new IOException("y"); });
   // onFailure gets the IOException (not an ExecutionException); failureCount() == 2
   // f.get() still throws ExecutionException with cause IOException
pool.submit(() -> 42);                 // nothing reported
Future<?> g = pool.submit(slowTask);
g.cancel(true);                        // cancelled: nothing reported
```

## Run
`mvn -q test -Dtest='C04*'`

## Done when
All `C04*` tests pass.

## Think about
- `execute(Runnable)` and `submit(Runnable)` treat an exception differently. Where does each one put it? Why does `afterExecute` get a non-null `Throwable` in one case and null in the other?
- A `Future` that is never asked for its result: what happens to the failure inside it? How many production systems lose errors this way?
- Why must you check `isDone()` before `get()` in `afterExecute`? What would a cancelled task do to a blocking `get()`?
