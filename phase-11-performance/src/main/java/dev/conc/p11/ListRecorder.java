package dev.conc.p11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Given. A simple exact recorder: keeps every sample, sorts to answer. Fine for tests, too heavy for production. */
public final class ListRecorder implements Recorder {
    private final List<Long> samples = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void record(long nanos) {
        samples.add(nanos);
    }

    public int count() {
        return samples.size();
    }

    /** Smallest sample such that at least p percent of the samples are not larger. */
    public long percentileNanos(double p) {
        List<Long> sorted;
        synchronized (samples) {
            sorted = new ArrayList<>(samples);
        }
        if (sorted.isEmpty()) return 0;
        Collections.sort(sorted);
        int rank = (int) Math.ceil(p / 100.0 * sorted.size());
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, rank - 1)));
    }

    public long maxNanos() {
        synchronized (samples) {
            return samples.stream().mapToLong(Long::longValue).max().orElse(0);
        }
    }
}
