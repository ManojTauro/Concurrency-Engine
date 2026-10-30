package dev.conc.p11;

import java.util.List;
import java.util.OptionalDouble;

/**
 * Challenge 4, part 1. Where does latency leave the flat part of the curve?
 *
 * find(points, factor): the points are in strictly ascending arrival rate (otherwise
 * IllegalArgumentException). The baseline is the p99 of the FIRST point. Return the arrival rate of
 * the first point whose p99 is strictly greater than factor * baseline, or empty if there is none
 * (or the list is empty). factor must be above 1, otherwise IllegalArgumentException.
 */
public final class Knee {

    private Knee() {
    }

    public static OptionalDouble find(List<RatePoint> points, double factor) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
