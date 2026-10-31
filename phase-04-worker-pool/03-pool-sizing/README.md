# 03 Pool sizing

## The problem
How many threads should a pool have? Too few and the CPU idles while tasks wait; too many and threads fight over the CPU and memory. The answer depends on whether tasks compute (CPU-bound) or wait (blocking: database, network). You write the rules of thumb as three small functions, then measure throughput for different pool sizes on your own machine and compare.

## What you implement
In `src/main/java/dev/conc/p4/`:
1. `Sizing` (three pure functions):
   - `cpuBound(int cores)`: threads for work that never waits: cores + 1.
   - `blocking(int cores, double targetUtilisation, double waitNanos, double computeNanos)`: `cores * targetUtilisation * (1 + wait / compute)`, rounded **up**, never below 1. `targetUtilisation` is in (0, 1].
   - `littlesLaw(double arrivalsPerSecond, double secondsPerRequest)`: average requests in the system, `L = arrivals per second * seconds per request`, rounded **up**.
2. `Sweep.run(Runnable task, int[] workerCounts, int tasks)`: for each size, in the given order, make a fixed pool of that many threads named `p4-sweep-1`, `p4-sweep-2`, ..., submit `tasks` copies of the task, wait until all are done, shut the pool down, and report tasks per second as `List<Sweep.Point(workers, tasksPerSecond)>`.

`Workloads` (given) has `cpuBurn(iterations)` and `sleep(millis)`.

## Rules
- `Sweep`: no `p4-sweep-*` thread is alive when `run` returns; `tasks < 1` throws `IllegalArgumentException`; an empty `workerCounts` returns an empty list.
- Work that waits must scale with more threads: sleeping tasks on 8 workers should be clearly faster than on 1.

## Examples
```java
Sizing.cpuBound(8);                       // 9
Sizing.blocking(8, 1.0, 1_000_000, 1_000_000);   // 16   (wait = compute -> 2x cores)
Sizing.blocking(8, 0.5, 10_000_000, 1_000_000);  // 44   (8 * 0.5 * 11)
Sizing.blocking(1, 0.1, 0, 1_000);        // 1    (0.1 rounds up to 1, never 0)
Sizing.littlesLaw(1_000, 0.05);           // 50   (1000 req/s each taking 50 ms: 50 in flight)
Sizing.littlesLaw(0, 0.5);                // 0

Sweep.run(Workloads.sleep(10), new int[]{1, 8}, 160)
// -> [Point(1, below 100/s), Point(8, below 800/s)]
//    each task sleeps 10 ms, so one worker can finish at most 100 tasks/s and eight at most 800/s
```

## Run
`mvn -q test -Dtest='C03*'`

## Done when
All `C03*` tests pass, and `../results/<date>-pool-sizing.md` exists with the table below.

## Then, for the note (`../results/<date>-pool-sizing.md`)
- Hypothesis first: for `cpuBurn` how many workers will give the best throughput on your machine? For `sleep(10)`?
- Run the sweep with 1, 2, 4, 8, 16, 32 workers for each workload and put the numbers in a table. Where is the knee? What happens after it?
- Compare with the formulas: does `Sizing.cpuBound` predict the CPU knee? Does `Sizing.blocking` predict the waiting one?
- Use Little's law to work out how many workers a service needs at 500 requests/s when a request takes 40 ms.

## Think about
What is the third resource that pool size has to respect besides the CPU (connections, memory, the downstream service)?
