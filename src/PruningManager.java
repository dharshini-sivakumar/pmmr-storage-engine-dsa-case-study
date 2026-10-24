package pmmr;

import java.util.BitSet;

/**
 * Manages logical pruning, death propagation, and compaction removability rules for a PMMR.
 *
 * Death Propagation Rule:
 * A node is dead if it is a spent leaf, or if both of its children are dead.
 *
 * Compaction Removability Invariant:
 * A node n is physically removable if and only if dead(n) AND dead(parent(n)).
 * Dead nodes whose parents are still alive MUST be retained as sibling proof witnesses.
 */
public final class PruningManager {
    private final BitSet dead;
    private long prunedLeaves;

    public PruningManager() {
        this.dead = new BitSet();
        this.prunedLeaves = 0;
    }

    public PruningManager(BitSet dead, long prunedLeaves) {
        this.dead = (BitSet) dead.clone();
        this.prunedLeaves = prunedLeaves;
    }

    /**
     * Logically prunes (spends) a leaf and propagates death upwards through the tree.
     *
     * @param leafIndex 0-indexed leaf index
     * @param totalLeaves current total number of leaves in the MMR
     * @param currentSize current total number of nodes in the MMR
     * @return true if newly pruned, false if already pruned
     */
    public boolean prune(long leafIndex, long totalLeaves, long currentSize) {
        if (leafIndex < 0 || leafIndex >= totalLeaves) {
            throw new IllegalArgumentException("no such leaf: " + leafIndex);
        }
        long pos = MMRMath.leafPos(leafIndex);
        if (dead.get((int) pos)) return false;
        dead.set((int) pos);
        prunedLeaves++;
        long cur = pos;
        while (true) {
            long par = MMRMath.parent(cur);
            if (par > currentSize) break; // cur is a peak
            if (dead.get((int) MMRMath.sibling(cur))) {
                dead.set((int) par);
            } else {
                break;
            }
            cur = par;
        }
        return true;
    }

    /**
     * Checks if a leaf index is unspent (live).
     */
    public boolean isUnspent(long leafIndex, long totalLeaves) {
        return leafIndex >= 0 && leafIndex < totalLeaves && !dead.get((int) MMRMath.leafPos(leafIndex));
    }

    /**
     * Checks if a node at position pos is marked dead.
     */
    public boolean isDead(long pos) {
        return dead.get((int) pos);
    }

    /**
     * Compaction predicate: checks if a node at position pos can be physically removed.
     */
    public boolean isRemovable(long pos, long currentSize) {
        if (!dead.get((int) pos)) return false;
        long par = MMRMath.parent(pos);
        return par <= currentSize && dead.get((int) par);
    }

    /**
     * Truncates dead bits for reorgs / rollbacks beyond targetSize and recalculates prunedLeaves.
     */
    public void truncate(long targetSize, long targetLeafCount, long currentSize) {
        if (currentSize > targetSize) {
            dead.clear((int) targetSize + 1, (int) currentSize + 2);
        }
        long pruned = 0;
        for (long i = 0; i < targetLeafCount; i++) {
            if (dead.get((int) MMRMath.leafPos(i))) {
                pruned++;
            }
        }
        this.prunedLeaves = pruned;
    }

    public long getPrunedLeaves() {
        return prunedLeaves;
    }

    public BitSet getBitSet() {
        return (BitSet) dead.clone();
    }

    public void setBitSet(BitSet newDead, long newPrunedLeaves) {
        dead.clear();
        dead.or(newDead);
        this.prunedLeaves = newPrunedLeaves;
    }

    public void clear() {
        dead.clear();
        prunedLeaves = 0;
    }
}
