# 04 Knee and USL

## The problem
A latency curve is flat while the system has spare capacity, then bends sharply upward: the **knee**. And throughput versus workers follows models you can compute: Amdahl's law (the serial part limits speedup), Gunther's Universal Scalability Law (adds a coherency cost that makes throughput *fall* past a peak), and Little's law (how many requests are in the system). You write these as small pure functions, then use them on your own measurements.

## What you implement
In `src/main/java/dev/conc/p11/` (`RatePoint(ratePerSecond, p99Millis)` is given):
1. `Knee.find(List<RatePoint> points, double factor)` returning `OptionalDouble`.
2. `Scalability` with four functions:
   - `amdahlSpeedup(double parallelFraction, int n)`: `1 / ((1 - p) + p / n)`
   - `uslThroughput(double lambda, double sigma, double kappa, int n)`: `lambda * n / (1 + sigma * (n - 1) + kappa * n * (n - 1))`
   - `uslPeakWorkers(double sigma, double kappa)`
   - `littlesLawL(double arrivalsPerSecond, double secondsPerRequest)`: `arrivals * seconds`

## Rules
**`Knee.find`**
- `points` must be in strictly ascending arrival rate, otherwise `IllegalArgumentException`. `factor` must be above 1, otherwise `IllegalArgumentException`.
- The baseline is the p99 of the **first** point. Return the arrival rate of the first point whose p99 is **strictly greater** than `factor * baseline`, or `OptionalDouble.empty()` if there is none (or the list is empty, or has one point).

**`uslPeakWorkers`**
- The whole number of workers `n >= 1` at which `uslThroughput` is highest. The continuous peak is at `sqrt((1 - sigma) / kappa)`, but the answer must be the **better of the two whole numbers around it**.
- `kappa == 0` throws `IllegalArgumentException` (no peak). `sigma >= 1` returns `1`.

## Examples
Knee: rates and p99 in ms: `(100, 5) (200, 6) (300, 12) (400, 20) (500, 80)`. Baseline 5 ms.
```
Knee.find(curve, 3)    -> 400   (first p99 above 15 ms is 20 ms at rate 400)
Knee.find(curve, 1.2)  -> 300   (6 is not strictly greater than 5 * 1.2 = 6; 12 is)
Knee.find(curve, 100)  -> empty
Knee.find(curve, 1)    -> IllegalArgumentException
```
Models:
```
amdahlSpeedup(0.9, 1)                 = 1.0
amdahlSpeedup(0.9, 4)                 = 3.0769...    (1 / (0.1 + 0.225))
amdahlSpeedup(0.9, 1_000_000)         just under 10  (the serial 10% caps it)
uslThroughput(100, 0.05, 0.001, 1)    = 100.0
uslThroughput(100, 0.05, 0.001, 8)    = 800 / 1.406 = 569.0...
uslThroughput(100, 0, 0, 7)           = 700.0        (no contention, no coherency: linear)
uslPeakWorkers(0.05, 0.001)           = 31           (continuous peak 30.82; n=31 is slightly better than n=30)
uslPeakWorkers(1.0, 0.01)             = 1
littlesLawL(1_000, 0.05)              = 50.0
littlesLawL(500, 0.04)                = 20.0
```

## Run
`mvn -q test -Dtest='C04*'`

## Done when
All `C04*` tests pass and `../results/<date>-latency-knee.md` has your knee and USL fit.

## Think about
- Amdahl says speedup is limited by the serial fraction. The USL adds a second term, coherency, that makes throughput *fall* after a peak. What physical thing in your Phase 5 and Phase 3 experiments is the coherency cost (cache lines moving between cores, lock hand-offs)?
- Take your worker-sweep numbers for `cpuBurn` and fit `sigma` and `kappa` by trial and error so the USL curve matches. What values did you get, and what does a large `kappa` tell you to fix?
- Little's law says `L = lambda * W`. Use it on your knee: at the rate where p99 explodes, how many requests are in the system?
