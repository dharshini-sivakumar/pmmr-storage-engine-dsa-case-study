package pmmr;

/**
 * Dedicated tree navigation arithmetic for Merkle Mountain Ranges.
 * Computes node heights, parent positions, sibling positions, and child positions
 * using zero-pointer 1-indexed post-order arithmetic in O(log pos) or O(1).
 */
public final class MMRNavigator {
    private MMRNavigator() {}

    /** Height of the node at 1-indexed position pos (leaf = 0). O(log pos). */
    public static int height(long pos) {
        long p = pos;
        while ((p & (p + 1)) != 0) {
            p -= Long.highestOneBit(p) - 1; // jump into the left sub-mountain
        }
        return Long.bitCount(p) - 1;
    }

    /** True if pos is a leaf (height == 0). */
    public static boolean isLeaf(long pos) {
        return height(pos) == 0;
    }

    /** True if pos is a right child (its parent is the very next position). */
    public static boolean isRightChild(long pos) {
        return height(pos + 1) > height(pos);
    }

    /** Parent position (may exceed current MMR size when pos is a peak). */
    public static long parent(long pos) {
        int h = height(pos);
        return isRightChild(pos) ? pos + 1 : pos + (1L << (h + 1));
    }

    /** Sibling position. */
    public static long sibling(long pos) {
        int h = height(pos);
        long off = (1L << (h + 1)) - 1;
        return isRightChild(pos) ? pos - off : pos + off;
    }

    /**
     * Left and right children of an internal node at pos.
     * Returns null if pos is a leaf (height == 0).
     */
    public static long[] children(long pos) {
        int h = height(pos);
        if (h == 0) return null;
        long right = pos - 1;
        long left = pos - (1L << h);
        return new long[]{left, right};
    }
}
