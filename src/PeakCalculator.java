package pmmr;

import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated peak calculation logic for Merkle Mountain Ranges.
 * Every MMR is composed of a sequence of perfect binary trees whose sizes
 * correspond to decreasing powers of two (2^(k+1) - 1).
 */
public final class PeakCalculator {
    private PeakCalculator() {}

    /**
     * Peak positions (left to right = tallest to shortest) of an MMR with `size` nodes.
     */
    public static long[] peaks(long size) {
        List<Long> out = new ArrayList<>();
        if (size > 0) {
            long peakSize = Long.highestOneBit(size + 1) - 1;
            long left = size, prev = 0;
            while (peakSize > 0) {
                if (left >= peakSize) {
                    prev += peakSize;
                    out.add(prev);
                    left -= peakSize;
                }
                peakSize >>= 1;
            }
        }
        long[] r = new long[out.size()];
        for (int i = 0; i < r.length; i++) r[i] = out.get(i);
        return r;
    }
}
