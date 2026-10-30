# 01 Latency histogram

**Do:** implement `LatencyHistogram` with the layout suggested in the Javadoc (exact counters below 256, then 128 sub-buckets per power of two).

**Run:** `mvn -q test -Dtest='C01*'`. One test records 20 million samples and checks the heap did not grow.

**Think about:**
- Why 128 sub-buckets? Work out the largest relative error of a bucket and show it is below 1%.
- An average hides the tail; a p95 hides the worst 5%. Which percentile would you put in an SLO for a payment API, and which for a recommendation widget?
- You cannot average two p99s from two servers. Why not, and what can you do (merge the histograms)?
- `record` is called on the hot path of every request. What does it cost, and where could it contend? (Look at the `LongAdder` idea from Phase 5.)
