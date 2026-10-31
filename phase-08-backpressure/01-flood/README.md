# 01 Flood

## The problem
What happens when events arrive faster than they are handled, and nothing limits the queue? You build exactly that on purpose and watch the queue depth climb. The consumer is never idle and never fails, yet the system is failing: the queue (and the age of the oldest event) grows without limit until memory runs out.

## What you implement
`FloodExperiment.run(Duration duration, long producerPerSecond, long consumerPerSecond)` in `src/main/java/dev/conc/p8/`. It returns `FloodResult(List<Integer> depthSamples, long produced, long consumed)`.

## Rules
- Use an **unbounded** `LinkedBlockingQueue` (no capacity).
- A producer thread named `p8-producer` adds events at `producerPerSecond`. A value of 0 or less means as fast as it can (only use that with a small heap, it fills memory).
- A consumer thread named `p8-consumer` takes events at **no more than** `consumerPerSecond` (it may idle when the queue is empty, but it must not catch up faster than that rate).
- The calling thread records `queue.size()` every 50 ms for the whole duration.
- Then both threads are stopped and joined: no `p8-` thread is alive when `run` returns.
- `produced` and `consumed` count the events made and taken.

## Example
`run(Duration.ofSeconds(1), 5_000, 500)`:
```
produced    about 5000      (the producer's rate)
consumed    about 500       (the consumer's rate)
depthSamples  about 20 numbers (one per 50 ms), rising from near 0 to a few thousand by the end
```
`run(Duration.ofSeconds(1), 500, 5_000)` (consumer faster): the depth stays near 0 and `consumed` is close to `produced`.
`depthSamples` has roughly `duration / 50 ms` entries.

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass and `../results/<date>-flood.md` exists.

## Think about (answer in `../results/<date>-flood.md`)
- At 20,000 in and 1,000 out per second, how deep is the queue after 10 seconds? After a minute? How much memory is that?
- The consumer is never idle and never fails. So why is this a failure of the system? What happens to the *age* of the oldest queued event, and why does that matter more than the depth?
- Where does the first symptom show up in a real service (latency, GC, an `OutOfMemoryError`)? Which of them would an alert catch first?
- Evidence run: producer at 20,000/s and consumer at 1,000/s, plot the depth samples. Then an unthrottled producer (0) with `-Xmx64m`: record the time, the depth and the GC log when the JVM runs out.
