package dev.conc.p1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Deterministic source of events. Same seed, same stream. Later phases use it as the producer, so
 * it also decides how uneven the load is: with skew above zero a few users get most of the events.
 */
public final class EventGenerator {
    private final long seed;
    private final int users;
    private final int products;
    private final double skew;

    private long eventId = 1;

    // index represents a user
    // value at that index represents last seq/offset for that user
    private final long[] userSeq;

    // represents prefix sum of weights of users
    private final double[] cumulativeWeights;

    // sum of all weights, equal to the last entry of cumulativeWeights
    private double total;

    private final Random r;

    // any number >= 0 works as starting point
    private long timestampMillis = 1000;

    private final EventType[] allEvents = EventType.values();

    /**
     * @param seed     random seed
     * @param users    number of users, ids "user-0" .. "user-(users-1)"
     * @param products number of products, ids "product-0" .. "product-(products-1)"
     * @param skew     0 means every user is equally likely; above 0, user i has weight 1 / (i + 1)^skew
     */
    public EventGenerator(long seed, int users, int products, double skew) {
        if (users <= 0) throw new IllegalArgumentException("users must be > 0");
        if (products <= 0) throw new IllegalArgumentException("products must be > 0");
        // NaN fails every comparison, so "skew < 0" alone lets NaN through. Infinity is rejected
        // too: Math.pow(1, Infinity) is NaN in Java, which would make total NaN.
        if (!Double.isFinite(skew) || skew < 0)
            throw new IllegalArgumentException("skew must be a finite number >= 0, got " + skew);

        this.seed = seed;
        this.users = users;
        this.products = products;
        this.skew = skew;

        this.userSeq = new long[this.users];
        this.cumulativeWeights = new double[this.users];
        r = new Random(this.seed);

        calculateCumulativeWeights();
    }

    private void calculateCumulativeWeights() {
        double cur = 0.0;

        for (int i = 0; i < this.users; i++) {
            double denominator = Math.pow((i + 1), this.skew);
            double weight = 1 / denominator;
            cur += weight;

            cumulativeWeights[i] = cur;
        }

        this.total = cur;
    }

    public Event next() {
        double key = r.nextDouble() * total;
        int index = getIndex(key);

        String user = "user-" + index;
        String product = "product-" + r.nextInt(this.products);

        EventType type = this.allEvents[r.nextInt(this.allEvents.length)];

        timestampMillis += r.nextLong(100);

        long prevSeq = userSeq[index];
        long newSeq = prevSeq + 1;
        userSeq[index] = newSeq;

        return new Event(nextEventId(), user, product, type, timestampMillis, newSeq);
    }

    private int getIndex(double key) {
        int result = Arrays.binarySearch(cumulativeWeights, key);

        int index;

        // Ranges are half-open: user i owns [cumulative[i-1], cumulative[i]).
        // So the user is the index of the first entry strictly greater than key.
        if (result >= 0) {
            // key equals cumulative[result], which is the start of the next user's range
            index = result + 1;
        } else {
            // not found: BS returned -(insertion_point) - 1, and the insertion point
            // is already the first entry greater than key. Decode: p = -(result + 1)
            index = -(result + 1);
        }

        // index == users can happen two ways: key >= total from floating-point rounding
        // (insertion point = length), or key == total exactly (found at last, then +1).
        // Both sit at the top of the line, so clamp to the last user rather than wrap.
        if (index == this.users) index = index - 1;
        return index;
    }

    public List<Event> take(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        List<Event> events = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            events.add(next());
        }

        return events;
    }

    private long nextEventId() {
        return this.eventId++;
    }
}
