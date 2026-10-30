package dev.conc.p11;

/**
 * Challenge 1. A latency histogram with fixed memory and a bounded relative error, in the spirit
 * of HdrHistogram (you do not need it as a dependency; read how it works afterwards).
 *
 *  - record(nanos): safe from many threads, no lock needed. Values below 0 count as 0, values
 *    above 2^40 (about 18 minutes) count as 2^40;
 *  - count(), min(), max() are EXACT (max() and min() report the clamped values);
 *  - percentile(p) for 0 < p <= 100: the smallest recorded value v (to within a relative error of
 *    1%) such that at least p percent of the recorded values are <= v. percentile(100) is exactly
 *    max(). It is never below min() or above max(). An empty histogram answers 0 for all of them;
 *  - merge(other) adds the samples of another histogram into this one;
 *  - memory must NOT grow with the number of samples.
 *
 * Suggested layout: values below 256 each get their own counter; above that, for a value v with
 * s = (number of bits of v) - 8, the bucket is (s, v >> s), which has 128 sub-buckets per power
 * of two. Why does that give a relative error below 1%?
 */
public final class LatencyHistogram {

    public LatencyHistogram() {
        // TODO
    }

    public void record(long nanos) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long count() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long min() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long max() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public long percentile(double p) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void merge(LatencyHistogram other) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
