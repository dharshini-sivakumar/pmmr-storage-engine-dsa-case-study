package pmmr;

import java.util.function.LongPredicate;

/**
 * Interface defining the contract for underlying MMR node storage.
 * Supports appending nodes in strictly increasing position order,
 * binary search queries, invariant-preserving physical compaction, and rewind truncation.
 */
public interface INodeStore {

    /**
     * Appends a position and its corresponding hash.
     * Positions must be strictly monotonically increasing.
     *
     * @param position 1-indexed MMR node position
     * @param hash     32-byte cryptographic hash
     */
    void append(long position, byte[] hash);

    /**
     * Retrieves the 32-byte hash stored at the given position.
     *
     * @param position 1-indexed MMR node position
     * @return 32-byte hash if present, or null if pruned/compacted
     */
    byte[] get(long position);

    /**
     * Checks if a node at the specified position is currently present in storage.
     *
     * @param position 1-indexed MMR node position
     * @return true if stored, false if pruned or absent
     */
    boolean contains(long position);

    /**
     * Physically compacts storage by removing entries matching the removable predicate.
     * Shrinks memory footprint and reclaims space.
     *
     * @param removable predicate identifying entries safe for removal
     * @return number of nodes physically removed
     */
    int compact(LongPredicate removable);

    /**
     * Truncates the storage to retain only positions <= maxPos (for reorgs and rollbacks).
     *
     * @param maxPos maximum position to keep
     */
    void truncate(long maxPos);

    /**
     * Resets the store, clearing all stored nodes and resetting capacity.
     */
    void clear();

    /**
     * Returns the total count of currently stored nodes.
     *
     * @return number of stored nodes
     */
    int count();

    /**
     * Returns the position stored at physical index i.
     *
     * @param index physical array index [0, count - 1]
     * @return 1-indexed MMR position
     */
    long posAt(int index);

    /**
     * Returns the 32-byte hash stored at physical index i.
     *
     * @param index physical array index [0, count - 1]
     * @return 32-byte hash
     */
    byte[] hashAt(int index);

    /**
     * Returns an array of all stored positions in ascending order.
     *
     * @return array of stored positions
     */
    long[] positions();

    /**
     * Calculates the raw payload bytes currently stored (8 bytes pos + 32 bytes hash per node).
     *
     * @return total payload bytes
     */
    long payloadBytes();
}
