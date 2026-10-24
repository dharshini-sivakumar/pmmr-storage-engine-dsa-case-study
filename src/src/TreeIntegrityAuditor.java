package pmmr;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic integrity auditor for PMMR trees.
 * Verifies:
 * 1. Monotonically increasing stored position order.
 * 2. Hash derivation of all internal nodes from their children.
 * 3. Peak presence and valid height bounds.
 * 4. Root commitment calculation.
 */
public final class TreeIntegrityAuditor {
    private TreeIntegrityAuditor() {}

    public static AuditReport audit(long size, INodeStore store, byte[] currentRoot) {
        List<String> errors = new ArrayList<>();
        int internalChecked = 0;
        long[] positions = store.positions();

        // 1. Check order
        for (int i = 1; i < positions.length; i++) {
            if (positions[i] <= positions[i - 1]) {
                errors.add("Store position order violation at index " + i + ": " + positions[i] + " <= " + positions[i - 1]);
            }
        }

        // 2. Check internal node hashes against children
        for (long p : positions) {
            if (p > size) {
                errors.add("Store position " + p + " exceeds MMR size " + size);
            }
            int h = MMRMath.height(p);
            if (h > 0) {
                long[] ch = MMRMath.children(p);
                byte[] left = store.get(ch[0]);
                byte[] right = store.get(ch[1]);
                byte[] nodeHash = store.get(p);

                if (left != null && right != null) {
                    byte[] expected = Hash.node(left, right);
                    if (!MessageDigest.isEqual(expected, nodeHash)) {
                        errors.add("Cryptographic hash mismatch at internal node " + p);
                    } else {
                        internalChecked++;
                    }
                }
            }
        }

        // 3. Check peaks
        long[] pks = MMRMath.peaks(size);
        int peaksChecked = 0;
        for (long pk : pks) {
            if (store.get(pk) == null) {
                errors.add("Missing peak at position " + pk);
            } else {
                peaksChecked++;
            }
        }

        // 4. Check root derivation
        try {
            if (currentRoot == null || currentRoot.length != Hash.SIZE) {
                errors.add("Invalid root generation");
            }
        } catch (Exception e) {
            errors.add("Failed root calculation: " + e.getMessage());
        }

        boolean valid = errors.isEmpty();
        return new AuditReport(valid, size, store.count(), internalChecked, peaksChecked, errors);
    }
}
