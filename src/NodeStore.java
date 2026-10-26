package pmmr;

import java.util.Arrays;
import java.util.function.LongPredicate;

/**
 * High-performance array-based node storage: two parallel arrays (sorted positions, hashes).
 * Appends are always in increasing position order, so the position array stays sorted
 * and lookups use binary search. compact() physically drops removable entries.
 * truncate() allows rewinding/rolling back to an earlier size.
 */
public final class NodeStore implements INodeStore {
    private long[] pos = new long[1024];
    private byte[][] hash = new byte[1024][];
    private int count;

    /** Amortised O(1). */
    @Override
    public void append(long p, byte[] h) {
        if (count > 0 && p <= pos[count - 1]) throw new IllegalArgumentException("positions must increase");
        if (count == pos.length) {
            int newCap = count * 2;
            pos = Arrays.copyOf(pos, newCap);
            hash = Arrays.copyOf(hash, newCap);
        }
        pos[count] = p;
        hash[count++] = h;
    }

    /** O(log m), m = stored nodes. Returns null if the node is not stored (pruned). */
    @Override
    public byte[] get(long p) {
        int i = Arrays.binarySearch(pos, 0, count, p);
        return i < 0 ? null : hash[i];
    }

    @Override
    public boolean contains(long p) {
        return Arrays.binarySearch(pos, 0, count, p) >= 0;
    }

    /** O(m): rewrite arrays keeping only entries that are NOT removable. Returns number removed. */
    @Override
    public int compact(LongPredicate removable) {
        int w = 0;
        for (int r = 0; r < count; r++) {
            if (!removable.test(pos[r])) {
                pos[w] = pos[r];
                hash[w] = hash[r];
                w++;
            }
        }
        int removed = count - w;
        int cap = Math.max(1024, w);
        pos = Arrays.copyOf(pos, cap);       // shrink: gives memory back
        hash = Arrays.copyOf(hash, cap);
        count = w;
        return removed;
    }

    /** Truncate store to keep only positions <= maxPos (for rewinds/reorgs). */
    @Override
    public void truncate(long maxPos) {
        if (count == 0) return;
        int idx = Arrays.binarySearch(pos, 0, count, maxPos);
        if (idx >= 0) {
            count = idx + 1;
        } else {
            count = -(idx + 1);
        }
    }

    @Override
    public void clear() {
        count = 0;
        pos = new long[1024];
        hash = new byte[1024][];
    }

    @Override
    public int count() { return count; }
    
    @Override
    public long posAt(int i) { return pos[i]; }
    
    @Override
    public byte[] hashAt(int i) { return hash[i]; }

    /** Returns all stored positions in ascending order. */
    @Override
    public long[] positions() {
        return Arrays.copyOf(pos, count);
    }

    /** payload bytes: 8-byte position + 32-byte hash per stored node */
    @Override
    public long payloadBytes() { return (long) count * (8 + Hash.SIZE); }
}
