package pmmr;

/**
 * Pure position arithmetic facade for a Merkle Mountain Range.
 * Positions are 1-indexed and follow insertion order (post-order traversal):
 *
 *   height 2              7
 *                       /   \
 *   height 1          3       6        10
 *                    / \     / \      /  \
 *   height 0        1   2   4   5    8    9    11 ...
 *
 * Everything here is O(log n) or O(1) - no tree pointers are needed.
 */
public final class MMRMath {
    private MMRMath() {}

    /** Height of the node at 1-indexed position pos (leaf = 0). O(log pos). */
    public static int height(long pos) {
        return MMRNavigator.height(pos);
    }

    /** 1-indexed MMR position of the leafIndex-th leaf (0-indexed). O(1). */
    public static long leafPos(long leafIndex) {
        return LeafMapper.leafPos(leafIndex);
    }

    /** True if pos is a leaf (height == 0). */
    public static boolean isLeaf(long pos) {
        return MMRNavigator.isLeaf(pos);
    }

    /** 0-indexed leafIndex for a given leaf position pos. Returns -1 if not a leaf. */
    public static long leafIndex(long pos) {
        return LeafMapper.leafIndex(pos);
    }

    /** Total MMR nodes (leaves + internal) for an MMR with leafCount leaves. O(1). */
    public static long sizeForLeafCount(long leafCount) {
        return LeafMapper.sizeForLeafCount(leafCount);
    }

    /** True if pos is a right child (its parent is the very next position). */
    public static boolean isRightChild(long pos) {
        return MMRNavigator.isRightChild(pos);
    }

    /** Parent position (may exceed the current MMR size when pos is a peak). */
    public static long parent(long pos) {
        return MMRNavigator.parent(pos);
    }

    /** Sibling position. */
    public static long sibling(long pos) {
        return MMRNavigator.sibling(pos);
    }

    /**
     * Left and right children of an internal node at pos.
     * Returns null if pos is a leaf (height == 0).
     */
    public static long[] children(long pos) {
        return MMRNavigator.children(pos);
    }

    /** Peak positions (left to right = tallest to shortest) of an MMR with `size` nodes. */
    public static long[] peaks(long size) {
        return PeakCalculator.peaks(size);
    }

    /** Number of leaves in an MMR with `size` nodes. */
    public static long leafCount(long size) {
        return LeafMapper.leafCount(size);
    }
}
