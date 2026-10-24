package pmmr;

import java.security.MessageDigest;
import java.util.*;

/**
 * Aggregated / Multi-Leaf Merkle Proof for multiple leaves in an MMR.
 * Deduplicates overlapping sibling hashes and tree paths, significantly reducing proof size
 * compared to independent single-leaf Merkle proofs.
 */
public final class MultiProof implements Proof {
    public final long[] leafIndices;
    public final long mmrSize;
    public final Map<Long, byte[]> proofHashes;  // position -> sibling hash
    public final Map<Long, byte[]> peakHashes;   // peak position -> peak hash

    public MultiProof(long[] leafIndices, long mmrSize,
                      Map<Long, byte[]> proofHashes, Map<Long, byte[]> peakHashes) {
        this.leafIndices = leafIndices.clone();
        this.mmrSize = mmrSize;
        this.proofHashes = Collections.unmodifiableMap(new HashMap<>(proofHashes));
        this.peakHashes = Collections.unmodifiableMap(new HashMap<>(peakHashes));
    }

    @Override
    public long getMmrSize() {
        return mmrSize;
    }

    public long[] getLeafIndices() {
        return leafIndices.clone();
    }

    public int getProofHashesCount() {
        return proofHashes.size();
    }

    public int getPeakHashesCount() {
        return peakHashes.size();
    }

    /**
     * Stateless verification of multi-leaf proof.
     * Reconstructs the Merkle subtrees from leaf data and proof hashes up to peaks,
     * then bags the peaks and checks against expectedRoot.
     */
    public static boolean verify(MultiProof p, List<byte[]> leafDataList, byte[] expectedRoot) {
        if (leafDataList == null || leafDataList.size() != p.leafIndices.length) return false;
        long totalLeaves = MMRMath.leafCount(p.mmrSize);

        Map<Long, byte[]> computed = new HashMap<>();
        TreeSet<Long> active = new TreeSet<>();

        for (int i = 0; i < p.leafIndices.length; i++) {
            long lidx = p.leafIndices[i];
            if (lidx < 0 || lidx >= totalLeaves) return false;
            long pos = MMRMath.leafPos(lidx);
            computed.put(pos, Hash.leaf(leafDataList.get(i)));
            active.add(pos);
        }

        while (!active.isEmpty()) {
            boolean anyUnderPeak = false;
            for (long pos : active) {
                if (MMRMath.parent(pos) <= p.mmrSize) {
                    anyUnderPeak = true;
                    break;
                }
            }
            if (!anyUnderPeak) break;

            TreeSet<Long> nextActive = new TreeSet<>();
            for (long u : active) {
                long par = MMRMath.parent(u);
                if (par > p.mmrSize) {
                    nextActive.add(u);
                    continue;
                }
                long sib = MMRMath.sibling(u);
                if (active.contains(sib)) {
                    if (u < sib) { // process left child
                        byte[] left = computed.get(u);
                        byte[] right = computed.get(sib);
                        if (left == null || right == null) return false;
                        computed.put(par, Hash.node(left, right));
                        nextActive.add(par);
                    }
                } else {
                    byte[] sibHash = p.proofHashes.get(sib);
                    if (sibHash == null) return false;
                    byte[] uHash = computed.get(u);
                    if (uHash == null) return false;
                    byte[] parHash = MMRMath.isRightChild(u)
                            ? Hash.node(sibHash, uHash)
                            : Hash.node(uHash, sibHash);
                    computed.put(par, parHash);
                    nextActive.add(par);
                }
            }
            active = nextActive;
        }

        long[] expectedPeaks = MMRMath.peaks(p.mmrSize);
        byte[][] peakArray = new byte[expectedPeaks.length][];
        for (int i = 0; i < expectedPeaks.length; i++) {
            long pk = expectedPeaks[i];
            if (computed.containsKey(pk)) {
                peakArray[i] = computed.get(pk);
            } else if (p.peakHashes.containsKey(pk)) {
                peakArray[i] = p.peakHashes.get(pk);
            } else {
                return false;
            }
        }

        byte[] root = PMMR.bagPeaks(p.mmrSize, peakArray);
        return MessageDigest.isEqual(root, expectedRoot);
    }
}
