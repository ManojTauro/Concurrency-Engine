# 03 Worker sweep

## The problem
How do throughput and latency change as you add workers to a pool? You run the same batch of work on pools of 1, 2, 4, 8, 16, 32 threads and record, for each size, how many tasks per second finished and how long tasks took from submission to completion (that includes waiting in the queue). The result is a table you can plot and compare with the models of challenge 4.

## What you implement
In `src/main/java/dev/conc/p11/` (`SweepPoint(workers, throughputPerSecond, p50Nanos, p99Nanos)`, `Work.cpuBurn(iterations)` and `Work.sleepMillis(ms)` are given):
- `WorkerSweep.run(Runnable work, int[] workerCounts, int events)` returning `List<SweepPoint>`
- `WorkerSweep.toCsv(List<SweepPoint> points)`

## Rules
`run`: for each count in `workerCounts` (in order):
- create a fixed pool of that many threads named `p11-sweep-1`, ... with an unbounded queue;
- at one instant submit `events` copies of the work, **all together**;
- each task records its **sojourn time** (completion minus the moment it was submitted) in a `ListRecorder`;
- `throughput = events / (time from the first submission until the last task completed)`;
- return `SweepPoint(workers, throughput, p50, p99)`.
- No `p11-` thread is alive afterwards. `events < 1` throws `IllegalArgumentException`. An empty `workerCounts` returns an empty list.

`toCsv`: first line `workers,throughput_per_s,p50_ms,p99_ms`, then one line per point, joined with `\n`, **no trailing newline**. Use `Locale.ROOT`; throughput with 1 decimal, the two latencies in milliseconds with 3 decimals.

## Example
```
toCsv(List.of(
    new SweepPoint(1, 198.0,  2_500_000, 31_250_000),
    new SweepPoint(8, 1523.4, 1_000_000,  4_001_000)))
->
workers,throughput_per_s,p50_ms,p99_ms
1,198.0,2.500,31.250
8,1523.4,1.000,4.001

toCsv(List.of())   ->   workers,throughput_per_s,p50_ms,p99_ms      (header only)
```
50 tasks of 10 ms on one worker cannot finish faster than 100 tasks/s, so the measured throughput is just under 100.

## Run
`mvn -q test -Dtest='C03*'`. Then run your sweep for real: 1, 2, 4, 8, 16, 32 workers, with `Work.cpuBurn(200_000)` and with `Work.sleepMillis(5)`, 2,000 events each.

## Done when
All `C03*` tests pass and `../results/<date>-worker-sweep.md` exists (CSV, `nproc`, JDK, where the curve flattens).

## Think about
- `cpuBurn`: where does throughput stop rising, and how does that relate to `nproc`? What happens to the p99 after the knee?
- `sleepMillis`: why does it scale far past the core count? What is the limit then?
- The sojourn time includes the wait in the queue. Why does p99 *fall* when you add workers at first, and what does that tell you about where latency comes from?
- What did you control (CPU frequency, other programs, JIT warm-up, GC)? Run the sweep twice: is it repeatable?
