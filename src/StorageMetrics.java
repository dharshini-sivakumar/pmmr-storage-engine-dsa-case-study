package pmmr;

/**
 * Encapsulates performance, capacity, and compression statistics for the PMMR storage engine.
 */
public final class StorageMetrics {
    private final long totalNodes;
    private final long totalLeaves;
    private final long unspentLeaves;
    private final long prunedLeaves;
    private final int storedNodes;
    private final long storedBytes;
    private final long fullBytes;
    private final double savingsPercent;

    public StorageMetrics(long totalNodes, long totalLeaves, long unspentLeaves,
                          long prunedLeaves, int storedNodes, long storedBytes, long fullBytes) {
        this.totalNodes = totalNodes;
        this.totalLeaves = totalLeaves;
        this.unspentLeaves = unspentLeaves;
        this.prunedLeaves = prunedLeaves;
        this.storedNodes = storedNodes;
        this.storedBytes = storedBytes;
        this.fullBytes = fullBytes;
        this.savingsPercent = 100.0 * (1.0 - (double) storedBytes / Math.max(1, fullBytes));
    }

    public long getTotalNodes() { return totalNodes; }
    public long getTotalLeaves() { return totalLeaves; }
    public long getUnspentLeaves() { return unspentLeaves; }
    public long getPrunedLeaves() { return prunedLeaves; }
    public int getStoredNodes() { return storedNodes; }
    public long getStoredBytes() { return storedBytes; }
    public long getFullBytes() { return fullBytes; }
    public double getSavingsPercent() { return savingsPercent; }

    @Override
    public String toString() {
        return String.format(
                "nodes=%d leaves=%d unspent=%d pruned=%d | stored nodes=%d | stored=%d B vs full=%d B (%.1f%% saved)",
                totalNodes, totalLeaves, unspentLeaves, prunedLeaves, storedNodes, storedBytes, fullBytes, savingsPercent);
    }
}
