# 01 Latency histogram

## The problem
To report "p99 latency" you need to remember millions of request times without storing them all. A **histogram** counts how many samples fall in each bucket; fixed memory, and percentiles are read off the counts. You build one in the spirit of HdrHistogram: bucket widths grow with the value so the error stays below 1%, whatever the size of the value.

## What you implement
`LatencyHistogram` in `src/main/java/dev/conc/p11/`:
- `void record(long nanos)`
- `long count()`, `long min()`, `long max()`
- `long percentile(double p)`
- `void merge(LatencyHistogram other)`

## Rules
- `record` is safe from many threads, with no lock needed. Values below 0 count as 0; values above `2^40` (about 18 minutes) count as `2^40`.
- `count()`, `min()`, `max()` are **exact** (min and max report the clamped values).
- `percentile(p)` for `0 < p <= 100`: the smallest recorded value `v` (to within a relative error of 1%) such that at least `p` percent of the recorded values are `<= v`. `percentile(100)` is **exactly** `max()`. Never below `min()` or above `max()`.
- An empty histogram answers `0` for `count`, `min`, `max` and every percentile.
- `merge(other)` adds the samples of another histogram into this one.
- Memory must **not** grow with the number of samples (a test records 20 million and checks the heap).
- Suggested layout (see the Javadoc): values below 256 each get their own counter; above that, for a value `v` with `s = (number of bits of v) - 8`, the bucket is `(s, v >> s)`, which gives 128 sub-buckets per power of two.

## Example
```java
var h = new LatencyHistogram();
h.record(100); h.record(200);
h.count();            // 2
h.percentile(50);     // 100   (half of the samples are <= 100)
h.percentile(100);    // 200   (exactly max)

// record 1..100_000_000 once each (1 ns to 100 ms):
h.percentile(50);     // about 50_000_000   (within 1%)
h.percentile(99);     // about 99_000_000
h.percentile(99.9);   // about 99_900_000

// 98 samples of 1 ms and 2 samples of 1 s:
h.percentile(98);     // about 1 ms
h.percentile(99.5);   // about 1 s     (the slow tail shows only in the high percentiles)

h.record(-5);  h.record(Long.MAX_VALUE);   // clamped to 0 and 2^40
```

## Run
`mvn -q test -Dtest='C01*'`. One test records 20 million samples and checks the heap did not grow.

## Done when
All `C01*` tests pass.

## Think about
- Why 128 sub-buckets? Work out the largest relative error of a bucket and show it is below 1%.
- An average hides the tail; a p95 hides the worst 5%. Which percentile would you put in an SLO for a payment API, and which for a recommendation widget?
- You cannot average two p99s from two servers. Why not, and what can you do (merge the histograms)?
- `record` is called on the hot path of every request. What does it cost, and where could it contend? (Look at the `LongAdder` idea from Phase 5.)
