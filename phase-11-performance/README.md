# Phase 11: Performance experiments

How fast is it, how does it scale, and what happens when you push it? Most performance numbers in blog posts are wrong in one of a few ways. This phase builds the tools to measure honestly: a histogram that keeps the tail, load generators that do not hide stalls, a sweep over worker counts, and the models that explain the curve.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-latency-histogram`: fixed memory, p50 to p99.9, error under 1% | `mvn -q test -Dtest='C01*'` |
| 2 | `02-load-generators`: closed loop versus open loop; coordinated omission | `mvn -q test -Dtest='C02*'` |
| 3 | `03-worker-sweep`: throughput and latency from 1 to 32 workers, as CSV | `mvn -q test -Dtest='C03*'` |
| 4 | `04-knee-and-usl`: find the knee; Amdahl, the Universal Scalability Law, Little's law | `mvn -q test -Dtest='C04*'` |

There are no domain classes here: the types you work on (`Service`, `Recorder`, `Work`, `ListRecorder`, `RatePoint`, `SweepPoint`) are given. Your code is in `src/main/java/dev/conc/p11/`; replace each `TODO`. The C02 test takes a few seconds on purpose.

**Read first (once):** Gil Tene, *How NOT to Measure Latency* (talk) and the HdrHistogram README (github.com/HdrHistogram/HdrHistogram); Neil Gunther, the Universal Scalability Law (perfdynamics.com); JCIP ch. 11 (performance and scalability) and the section on Amdahl's law; Brendan Gregg, *Systems Performance* ch. 2 (methodologies) and the USE method; async-profiler README and the JMH samples (openjdk/jmh) before you trust a microbenchmark. Check the references.

**Evidence:** this phase is the one that produces the most posts. Run each of the following and write a note with the hypothesis first:
1. `../results/<date>-coordinated-omission.md`: the stalling service from the C02 test at 500 req/s, measured closed-loop and open-loop, histograms side by side.
2. `../results/<date>-worker-sweep.md`: the sweep from 1 to 32 workers for `cpuBurn` and for `sleepMillis`, with the CSV, `nproc`, the JDK, and the knee. Say where the curve flattens and fit the USL by hand.
3. `../results/<date>-latency-knee.md`: use your open-loop generator against your Phase 4 pool or Phase 8 ingest, rising rate, find the rate where p99 explodes, check the GC log (`-Xlog:gc*`) at that point.
