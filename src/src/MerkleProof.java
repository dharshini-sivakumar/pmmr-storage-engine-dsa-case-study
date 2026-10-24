package pmmr;

import java.security.MessageDigest;
import java.util.List;

/** Inclusion proof for one leaf: sibling path up to its peak + all peak hashes. Size is O(log n). */
public final class MerkleProof implements Proof {
    public final long leafIndex;
    public final long mmrSize;
    public final List<byte[]> siblings;
    public final byte[][] peaks;

    public MerkleProof(long leafIndex, long mmrSize, List<byte[]> siblings, byte[][] peaks) {
        this.leafIndex = leafIndex;
        this.mmrSize = mmrSize;
        this.siblings = siblings;
        this.peaks = peaks;
    }

    @Override
    public long getMmrSize() {
        return mmrSize;
    }

    public long getLeafIndex() {
        return leafIndex;
    }

    public int getSiblingCount() {
        return siblings.size();
    }

    /**
     * Stateless verification - needs only the proof, the leaf data and the trusted root.
     * The left/right orientation of every step is re-derived from leafIndex and mmrSize,
     * so a malicious prover cannot choose it. O(log n).
     */
    public static boolean verify(MerkleProof p, byte[] leafData, byte[] expectedRoot) {
        if (p.leafIndex < 0 || p.leafIndex >= MMRMath.leafCount(p.mmrSize)) return false;
        byte[] h = Hash.leaf(leafData);
        long cur = MMRMath.leafPos(p.leafIndex);
        int i = 0;
        while (MMRMath.parent(cur) <= p.mmrSize) {
            if (i >= p.siblings.size()) return false;
            byte[] sib = p.siblings.get(i++);
            h = MMRMath.isRightChild(cur) ? Hash.node(sib, h) : Hash.node(h, sib);
            cur = MMRMath.parent(cur);
        }
        if (i != p.siblings.size()) return false;

        long[] peakPos = MMRMath.peaks(p.mmrSize);
        if (peakPos.length != p.peaks.length) return false;
        int idx = -1;
        for (int k = 0; k < peakPos.length; k++) if (peakPos[k] == cur) idx = k;
        if (idx < 0) return false;

        byte[][] pk = p.peaks.clone();
        pk[idx] = h;
        byte[] root = PMMR.bagPeaks(p.mmrSize, pk);
        return MessageDigest.isEqual(root, expectedRoot);
    }
}
