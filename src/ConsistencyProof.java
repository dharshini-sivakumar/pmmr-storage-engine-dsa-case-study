package pmmr;

import java.security.MessageDigest;
import java.util.*;

/**
 * Consistency / Historical Range Proof.
 * Proves that an earlier MMR state at size fromSize (with fromRoot)
 * is a strict, immutable historical prefix of the MMR at toSize (with toRoot).
 */
public final class ConsistencyProof {
    public final long fromSize;
    public final long toSize;
    public final byte[][] oldPeaks;
    public final byte[][] newPeaks;
    public final Map<Long, List<byte[]>> oldPeakPaths; // old peak pos -> sibling path to enclosing new peak

    public ConsistencyProof(long fromSize, long toSize,
                            byte[][] oldPeaks,
                            byte[][] newPeaks,
                            Map<Long, List<byte[]>> oldPeakPaths) {
        this.fromSize = fromSize;
        this.toSize = toSize;
        this.oldPeaks = oldPeaks.clone();
        this.newPeaks = newPeaks.clone();
        Map<Long, List<byte[]>> copy = new HashMap<>();
        for (Map.Entry<Long, List<byte[]>> e : oldPeakPaths.entrySet()) {
            copy.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
        }
        this.oldPeakPaths = Collections.unmodifiableMap(copy);
    }

    /**
     * Stateless verification:
     * 1. Confirms oldPeaks bag to expectedFromRoot at fromSize.
     * 2. Confirms newPeaks bag to expectedToRoot at toSize.
     * 3. For every historical peak in fromSize, verifies it climbs deterministically
     *    via Merkle path to match the corresponding peak in toSize.
     */
    public static boolean verify(ConsistencyProof proof, byte[] expectedFromRoot, byte[] expectedToRoot) {
        if (proof.fromSize > proof.toSize || proof.fromSize < 0) return false;

        // 1. Verify old root
        if (proof.fromSize == 0) {
            byte[] emptyRoot = PMMR.bagPeaks(0, new byte[0][]);
            if (!MessageDigest.isEqual(emptyRoot, expectedFromRoot)) return false;
        } else {
            long[] fromPeakPos = MMRMath.peaks(proof.fromSize);
            if (fromPeakPos.length != proof.oldPeaks.length) return false;
            byte[] computedOldRoot = PMMR.bagPeaks(proof.fromSize, proof.oldPeaks);
            if (!MessageDigest.isEqual(computedOldRoot, expectedFromRoot)) return false;
        }

        // 2. Verify new root
        long[] toPeakPos = MMRMath.peaks(proof.toSize);
        if (toPeakPos.length != proof.newPeaks.length) return false;
        byte[] computedNewRoot = PMMR.bagPeaks(proof.toSize, proof.newPeaks);
        if (!MessageDigest.isEqual(computedNewRoot, expectedToRoot)) return false;

        if (proof.fromSize == 0 || proof.fromSize == proof.toSize) return true;

        // 3. For each old peak, verify that it matches or climbs to the correct peak in newPeaks
        Map<Long, byte[]> newPeakMap = new HashMap<>();
        for (int i = 0; i < toPeakPos.length; i++) {
            newPeakMap.put(toPeakPos[i], proof.newPeaks[i]);
        }

        long[] fromPeakPos = MMRMath.peaks(proof.fromSize);
        for (int i = 0; i < fromPeakPos.length; i++) {
            long p = fromPeakPos[i];
            byte[] curHash = proof.oldPeaks[i];

            if (newPeakMap.containsKey(p)) {
                if (!MessageDigest.isEqual(curHash, newPeakMap.get(p))) return false;
            } else {
                List<byte[]> path = proof.oldPeakPaths.get(p);
                if (path == null) return false;
                long cur = p;
                int step = 0;
                while (!newPeakMap.containsKey(cur) && cur <= proof.toSize) {
                    if (step >= path.size()) return false;
                    byte[] sibHash = path.get(step++);
                    curHash = MMRMath.isRightChild(cur)
                            ? Hash.node(sibHash, curHash)
                            : Hash.node(curHash, sibHash);
                    cur = MMRMath.parent(cur);
                }
                if (step != path.size() || !newPeakMap.containsKey(cur)) return false;
                if (!MessageDigest.isEqual(curHash, newPeakMap.get(cur))) return false;
            }
        }
        return true;
    }
}
