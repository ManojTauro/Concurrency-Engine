# 03 Drainable pipeline

## The problem
A pipeline accepts events and processes them on worker threads. When it is told to close, one promise matters: **every event for which `submit` said "yes" must be processed**. The danger is a race: `submit` checks "not closed yet", then `close` runs, then `submit` enqueues, and the event lands in a queue nobody will drain. You build a pipeline where "accepted" and "will be processed" are one atomic fact, even when `close` and `submit` run at the same moment on different threads.

## What you implement
`DrainablePipeline(int workers, int capacity, Consumer<Event> sink)` in `src/main/java/dev/conc/p10/`:
- `boolean submit(Event event) throws InterruptedException`
- `DrainReport close(Duration timeout) throws InterruptedException`
- `Thread shutdownHook(Duration timeout)`

`DrainReport(long accepted, long processed, boolean drained)` is given.

## Rules
- `workers` threads named `p10-worker-0`, `p10-worker-1`, ... take events from a bounded queue of `capacity` and call the sink. A sink that throws does not stop the worker (the event still counts as processed).
- `submit` returns `true` when the event was accepted (it waits while the queue is full) and `false` when the pipeline is closing or closed; the event is then **not** accepted.
- **Every event for which `submit` returned `true` reaches the sink before `close` reports success**, even if `submit` and `close` race.
- `close(timeout)` stops accepting, lets the workers finish what is queued, ends the workers, and returns `DrainReport(accepted, processed, true)`. If that takes longer than `timeout`, it interrupts the workers and returns `drained = false` (`processed` may then be below `accepted`).
- `close` is **idempotent and safe from several threads at once**: every call returns the report of the first one.
- `shutdownHook(timeout)` returns an **unstarted** thread that calls `close(timeout)`, to register with `Runtime.getRuntime().addShutdownHook(...)`.
- `workers < 1` or `capacity < 1` throws `IllegalArgumentException`.

## Example
```
p = new DrainablePipeline(2, 100, sink)
5 000 x p.submit(e)             -> true each time
p.close(Duration.ofSeconds(5))  -> DrainReport(5000, 5000, true)    the sink saw all 5000
p.submit(e)                     -> false   (closed; e is not accepted)
p.close(...)                    -> the same DrainReport again

race: 4 threads keep submitting while another thread calls close:
   trues = number of submits that returned true
   report.accepted() == trues == report.processed() == events the sink saw

5 events accepted, the sink blocks forever, close(200 ms):
   -> DrainReport(5, processed < 5, drained = false) after about 200 ms, workers interrupted and gone
```

## Run
`mvn -q test -Dtest='C03*'`. The race test runs 10 rounds of four submitters against a `close`.

## Done when
All `C03*` tests pass and `../results/<date>-shutdown-race.md` exists.

## Think about
- "Check that we are not closed, then enqueue" is two steps. What can happen between them when another thread closes? How does your design make "accepted" and "will be processed" one atomic fact? (A read-write lock, a counter of in-flight submits, and a state machine are three answers: compare them.)
- A submitter is blocked on a full queue when `close` starts. Does it get in or get refused? Which of the two keeps the guarantee?
- How do the workers learn that no more events are coming? Compare poison pills with "queue empty and closing".
- `Runtime.addShutdownHook` threads run when the JVM gets SIGTERM, concurrently with each other, with no ordering. What can go wrong with a hook that takes 60 seconds when the orchestrator waits 30 before SIGKILL?
- Experiment: 4 submitters and `close` at a random moment, 1,000 times; count lost events with (a) a plain `volatile boolean closed` check followed by `put`, and (b) your version.
