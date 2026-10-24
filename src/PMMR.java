package pmmr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Advanced Prunable Merkle Mountain Range (PMMR) Storage Engine.
 *
 * Capabilities:
 *  - Append-only Merkle Mountain Range structure with perfect binary subtrees.
 *  - Domain-separated SHA-256 cryptographic hashing.
 *  - Single and batch appends (amortised O(1)).
 *  - Logical pruning (O(log n)) with bitset tracking.
 *  - Physical compaction (O(m)) preserving sibling proof witnesses and peaks.
 *  - Stateless Single-Leaf Inclusion Proofs.
 *  - Aggregated Multi-Leaf Proofs with sibling deduplication.
 *  - Consistency / Historical Range Proofs (verifying immutable prefix history).
 *  - Blockchain Reorg / Rewind support (O(1) / O(k)).
 *  - Full tree cryptographic integrity self-audit.
 *  - Thread-safe concurrency via ReentrantReadWriteLock.
 *  - ASCII Mountain Range Visualizer for inspections and evaluations.
 *  - High-performance binary file persistence.
 */
public class PMMR {
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private INodeStore store = new NodeStore();
    private final PruningManager pruningManager = new PruningManager();
    private long size; // number of nodes (leaves + parents)

    // ---------------------------------------------------------------- append
    /** Append a single leaf. Worst case O(log n) hashes, amortised O(1). Returns the leaf index. */
    public long append(byte[] data) {
        writeLock.lock();
        try {
            return appendInternal(data);
        } finally {
            writeLock.unlock();
        }
    }

    public long append(String data) {
        return append(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /** Batch append multiple leaves. Returns array of assigned leaf indices. */
    public long[] batchAppend(List<byte[]> dataList) {
        writeLock.lock();
        try {
            long[] indices = new long[dataList.size()];
            for (int i = 0; i < dataList.size(); i++) {
                indices[i] = appendInternal(dataList.get(i));
            }
            return indices;
        } finally {
            writeLock.unlock();
        }
    }

    private long appendInternal(byte[] data) {
        long leafIndex = MMRMath.leafCount(size);
        byte[] curHash = Hash.leaf(data);
        long cur = size + 1;
        store.append(cur, curHash);
        size = cur;

        // while the next slot is taller than the node we just wrote, we are a right child -> merge
        while (MMRMath.height(size + 1) > MMRMath.height(cur)) {
            byte[] left = store.get(MMRMath.sibling(cur));
            if (left == null) throw new IllegalStateException("missing left sibling (corrupt store)");
            curHash = Hash.node(left, curHash);
            cur = size + 1;
            store.append(cur, curHash);
            size = cur;
        }
        return leafIndex;
    }

    // ----------------------------------------------------------------- prune
    /** Logically prune (spend) a leaf. O(log n). Returns false if already pruned. */
    public boolean prune(long leafIndex) {
        writeLock.lock();
        try {
            return pruningManager.prune(leafIndex, MMRMath.leafCount(size), size);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isUnspent(long leafIndex) {
        readLock.lock();
        try {
            return pruningManager.isUnspent(leafIndex, MMRMath.leafCount(size));
        } finally {
            readLock.unlock();
        }
    }

    private boolean removable(long pos) {
        return pruningManager.isRemovable(pos, size);
    }

    // --------------------------------------------------------------- compact
    /** Physically drop removable nodes. O(m). Returns number of nodes removed. */
    public int compact() {
        writeLock.lock();
        try {
            return store.compact(this::removable);
        } finally {
            writeLock.unlock();
        }
    }

    // ---------------------------------------------------------------- rewind
    /**
     * Rewind / Rollback the MMR to an earlier leaf count (e.g., handling blockchain reorganizations).
     * Any nodes beyond the target leaf count are discarded.
     */
    public boolean rewind(long targetLeafCount) {
        writeLock.lock();
        try {
            long currentLeaves = leafCount();
            if (targetLeafCount < 0 || targetLeafCount > currentLeaves) {
                throw new IllegalArgumentException("targetLeafCount out of range [0, " + currentLeaves + "]: " + targetLeafCount);
            }
            if (targetLeafCount == currentLeaves) return true;

            long targetSize = MMRMath.sizeForLeafCount(targetLeafCount);
            store.truncate(targetSize);
            pruningManager.truncate(targetSize, targetLeafCount, size);
            this.size = targetSize;
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    // ------------------------------------------------------------------ root
    /** Combine peaks right-to-left, then bind the MMR size. */
    public static byte[] bagPeaks(long mmrSize, byte[][] peaks) {
        if (peaks.length == 0) return Hash.root(0, new byte[Hash.SIZE]);
        byte[] acc = peaks[peaks.length - 1];
        for (int i = peaks.length - 2; i >= 0; i--) {
            acc = Hash.node(peaks[i], acc);
        }
        return Hash.root(mmrSize, acc);
    }

    private byte[][] peakHashesInternal() {
        long[] pp = MMRMath.peaks(size);
        byte[][] out = new byte[pp.length][];
        for (int i = 0; i < pp.length; i++) {
            out[i] = store.get(pp[i]);
            if (out[i] == null) throw new IllegalStateException("peak missing at " + pp[i]);
        }
        return out;
    }

    /** Root commitment. O(log n) peaks (each looked up in O(log m)). Unaffected by pruning. */
    public byte[] root() {
        readLock.lock();
        try {
            return bagPeaks(size, peakHashesInternal());
        } finally {
            readLock.unlock();
        }
    }

    // ---------------------------------------------------------------- proofs
    /** Inclusion proof for an UNSPENT leaf. O(log n) steps. */
    public MerkleProof proof(long leafIndex) {
        readLock.lock();
        try {
            if (!isUnspent(leafIndex)) {
                throw new IllegalArgumentException("leaf " + leafIndex + " is pruned or does not exist");
            }
            long cur = MMRMath.leafPos(leafIndex);
            List<byte[]> sibs = new ArrayList<>();
            while (MMRMath.parent(cur) <= size) {
                byte[] s = store.get(MMRMath.sibling(cur));
                if (s == null) throw new IllegalStateException("sibling missing (corrupt store)");
                sibs.add(s);
                cur = MMRMath.parent(cur);
            }
            return new MerkleProof(leafIndex, size, sibs, peakHashesInternal());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Aggregated multi-leaf proof for multiple unspent leaves.
     * Eliminates duplicate sibling hashes across overlapping Merkle paths.
     */
    public MultiProof multiProof(List<Long> leafIndices) {
        readLock.lock();
        try {
            if (leafIndices == null || leafIndices.isEmpty()) {
                throw new IllegalArgumentException("leafIndices must not be empty");
            }

            TreeSet<Long> sortedUnique = new TreeSet<>(leafIndices);
            long[] indices = new long[sortedUnique.size()];
            int idx = 0;
            for (long l : sortedUnique) {
                if (!isUnspent(l)) throw new IllegalArgumentException("leaf " + l + " is pruned or does not exist");
                indices[idx++] = l;
            }

            Map<Long, byte[]> computed = new HashMap<>();
            TreeSet<Long> active = new TreeSet<>();
            for (long l : indices) {
                long pos = MMRMath.leafPos(l);
                byte[] h = store.get(pos);
                if (h == null) throw new IllegalStateException("leaf data missing at " + pos);
                computed.put(pos, h);
                active.add(pos);
            }

            Map<Long, byte[]> proofSiblings = new HashMap<>();

            while (!active.isEmpty()) {
                boolean anyUnderPeak = false;
                for (long pos : active) {
                    if (MMRMath.parent(pos) <= size) {
                        anyUnderPeak = true;
                        break;
                    }
                }
                if (!anyUnderPeak) break;

                TreeSet<Long> nextActive = new TreeSet<>();
                for (long u : active) {
                    long par = MMRMath.parent(u);
                    if (par > size) {
                        nextActive.add(u);
                        continue;
                    }
                    long sib = MMRMath.sibling(u);
                    if (active.contains(sib)) {
                        if (u < sib) {
                            byte[] left = computed.get(u);
                            byte[] right = computed.get(sib);
                            computed.put(par, Hash.node(left, right));
                            nextActive.add(par);
                        }
                    } else {
                        byte[] sibHash = store.get(sib);
                        if (sibHash == null) throw new IllegalStateException("missing sibling at " + sib);
                        proofSiblings.put(sib, sibHash);
                        byte[] uHash = computed.get(u);
                        byte[] parHash = MMRMath.isRightChild(u)
                                ? Hash.node(sibHash, uHash)
                                : Hash.node(uHash, sibHash);
                        computed.put(par, parHash);
                        nextActive.add(par);
                    }
                }
                active = nextActive;
            }

            long[] pks = MMRMath.peaks(size);
            Map<Long, byte[]> peakHashes = new HashMap<>();
            for (long pk : pks) {
                if (!computed.containsKey(pk)) {
                    byte[] ph = store.get(pk);
                    if (ph == null) throw new IllegalStateException("missing peak at " + pk);
                    peakHashes.put(pk, ph);
                }
            }

            return new MultiProof(indices, size, proofSiblings, peakHashes);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Consistency / Range Proof:
     * Generates a proof that earlier state at fromSize is an exact historical prefix of current state.
     */
    public ConsistencyProof consistencyProof(long fromSize) {
        readLock.lock();
        try {
            if (fromSize < 0 || fromSize > size) {
                throw new IllegalArgumentException("fromSize out of bounds [0, " + size + "]: " + fromSize);
            }

            long[] fromPeaks = MMRMath.peaks(fromSize);
            byte[][] oldPeakHashes = new byte[fromPeaks.length][];
            for (int i = 0; i < fromPeaks.length; i++) {
                byte[] h = store.get(fromPeaks[i]);
                if (h == null) throw new IllegalStateException("historical peak missing at " + fromPeaks[i]);
                oldPeakHashes[i] = h;
            }

            long[] toPeaks = MMRMath.peaks(size);
            byte[][] newPeakHashes = peakHashesInternal();
            Set<Long> toPeakSet = new HashSet<>();
            for (long p : toPeaks) toPeakSet.add(p);

            Map<Long, List<byte[]>> oldPeakPaths = new HashMap<>();

            for (long p : fromPeaks) {
                if (!toPeakSet.contains(p)) {
                    List<byte[]> path = new ArrayList<>();
                    long cur = p;
                    while (!toPeakSet.contains(cur) && cur <= size) {
                        long sib = MMRMath.sibling(cur);
                        byte[] sibHash = store.get(sib);
                        if (sibHash == null) throw new IllegalStateException("missing sibling at " + sib);
                        path.add(sibHash);
                        cur = MMRMath.parent(cur);
                    }
                    oldPeakPaths.put(p, path);
                }
            }

            return new ConsistencyProof(fromSize, size, oldPeakHashes, newPeakHashes, oldPeakPaths);
        } finally {
            readLock.unlock();
        }
    }

    // ------------------------------------------------------- integrity audit
    /**
     * Comprehensive cryptographic integrity audit.
     * Verifies store ordering, internal node hashes against children, peak presence, and root match.
     */
    public AuditReport validateIntegrity() {
        readLock.lock();
        try {
            return TreeIntegrityAuditor.audit(size, store, root());
        } finally {
            readLock.unlock();
        }
    }

    // ------------------------------------------------------------- visualizer
    /**
     * Render an ASCII diagram of the Merkle Mountain Range.
     */
    public String renderAsciiTree() {
        return renderAsciiTree(32);
    }

    public String renderAsciiTree(int maxLeaves) {
        readLock.lock();
        try {
            return TreeVisualizer.render(this, maxLeaves);
        } finally {
            readLock.unlock();
        }
    }

    // ----------------------------------------------------------------- stats
    public long size() { readLock.lock(); try { return size; } finally { readLock.unlock(); } }
    public long leafCount() { readLock.lock(); try { return MMRMath.leafCount(size); } finally { readLock.unlock(); } }
    public long prunedLeafCount() { readLock.lock(); try { return pruningManager.getPrunedLeaves(); } finally { readLock.unlock(); } }
    public long unspentLeafCount() { readLock.lock(); try { return leafCount() - prunedLeafCount(); } finally { readLock.unlock(); } }
    public int storedNodes() { readLock.lock(); try { return store.count(); } finally { readLock.unlock(); } }
    public long storedBytes() { readLock.lock(); try { return store.payloadBytes(); } finally { readLock.unlock(); } }
    public long fullBytes() { readLock.lock(); try { return size * (8 + Hash.SIZE); } finally { readLock.unlock(); } }

    public StorageMetrics getMetrics() {
        readLock.lock();
        try {
            return new StorageMetrics(size, leafCount(), unspentLeafCount(), prunedLeafCount(), storedNodes(), storedBytes(), fullBytes());
        } finally {
            readLock.unlock();
        }
    }

    public String stats() {
        readLock.lock();
        try {
            return getMetrics().toString();
        } finally {
            readLock.unlock();
        }
    }

    // ----------------------------------------------------------- persistence
    public void save(Path file) throws IOException {
        readLock.lock();
        try {
            PMMRSerializer.save(file, size, pruningManager.getPrunedLeaves(), pruningManager.getBitSet(), store);
        } finally {
            readLock.unlock();
        }
    }

    public static PMMR load(Path file) throws IOException {
        PMMR m = new PMMR();
        PMMRSerializer.load(file, m);
        return m;
    }

    // ------------------------------------------------ package helpers
    boolean hasNode(long pos) {
        return store.contains(pos);
    }

    boolean isDead(long pos) {
        return pruningManager.isDead(pos);
    }

    void restoreState(long restoredSize, long restoredPrunedLeaves, BitSet restoredDead, INodeStore restoredStore) {
        writeLock.lock();
        try {
            this.size = restoredSize;
            this.pruningManager.setBitSet(restoredDead, restoredPrunedLeaves);
            this.store = restoredStore;
        } finally {
            writeLock.unlock();
        }
    }
}
