# 03 Pipeline

## The problem
Wire a real producer-consumer pipeline: one thread reads a list of events and queues them, another thread takes them off the queue and hands each to a `sink` (a function that "handles" an event). Two things make this harder than it sounds:
1. **How does the consumer know it is finished?** The queue itself never says "no more".
2. **What if the sink throws halfway?** The producer may be blocked on a full queue, waiting for a consumer that is gone. The run must still end, promptly, with no thread left behind.

## What you implement
`EventPipeline.run(List<Event> source, int capacity, Consumer<Event> sink)` in `src/main/java/dev/conc/p2/`. Use an `ArrayBlockingQueue` of the given capacity. It returns `PipelineResult(consumed)`.

## Rules
- The producer thread is named `p2-producer`, the consumer thread `p2-consumer`.
- The sink is called only on the consumer thread, once per event, **in the order of `source`**.
- `run` returns after every event reached the sink, and no thread whose name starts with `p2-` is still alive.
- `consumed` is the number of events the sink handled. An empty source returns `consumed = 0` and does not hang.
- If the sink throws, `run` throws `PipelineFailedException` with the sink's exception as the **cause**, promptly, and again leaves no `p2-` thread alive. The sink is not called again after it threw.
- Works for capacity 1.

## Example
`source` = 10 000 events, capacity 4, and a sink that throws `IllegalStateException("boom")` on its 10th call:
```
sink handles events 1..9 normally
sink throws on event 10
run(...) throws PipelineFailedException, getCause() is the IllegalStateException("boom")
the sink was called exactly 10 times
no "p2-producer" thread is blocked on queue.put(...) afterwards
```
With a healthy sink and 3 events `[e1, e2, e3]`: the sink sees e1, e2, e3 in that order, `run` returns `PipelineResult(3)`.

## Run
`mvn -q test -Dtest='C03*'`

## Done when
All `C03*` tests pass.

## Think about
- How does the consumer learn that the producer is finished? Name two ways (a poison pill, a shared flag) and say what each one risks.
- The sink throws on the tenth event while the producer is blocked on a full queue. Who unblocks the producer?
- Which thread's exception do you report if both threads fail?
- What does `InterruptedException` mean in each thread, and what do you do with the interrupt flag?
