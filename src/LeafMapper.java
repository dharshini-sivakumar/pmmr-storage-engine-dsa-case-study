package pmmr;

/**
 * Dedicated mappings between 0-indexed leaf indices and 1-indexed MMR node positions.
 */
public final class LeafMapper {
    private LeafMapper() {}

    /** 1-indexed MMR position of the leafIndex-th leaf (0-indexed). O(1). */
    public static long leafPos(long leafIndex) {
        return 2 * leafIndex - Long.bitCount(leafIndex) + 1;
    }

    /** 0-indexed leafIndex for a given leaf position pos. Returns -1 if not a leaf. */
    public static long leafIndex(long pos) {
        if (!MMRNavigator.isLeaf(pos)) return -1;
        // Binary search for leafIndex in [0, pos]
        long lo = 0, hi = pos;
        while (lo <= hi) {
            long mid = (lo + hi) >>> 1;
            long p = leafPos(mid);
            if (p == pos) return mid;
            if (p < pos) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** Total MMR nodes (leaves + internal) for an MMR with leafCount leaves. O(1). */
    public static long sizeForLeafCount(long leafCount) {
        if (leafCount <= 0) return 0;
        return 2 * leafCount - Long.bitCount(leafCount);
    }

    /** Number of leaves in an MMR with `size` nodes. */
    public static long leafCount(long size) {
        long n = 0;
        for (long p : PeakCalculator.peaks(size)) {
            n += 1L << MMRNavigator.height(p);
        }
        return n;
    }
}
