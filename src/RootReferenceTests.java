package pmmr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Validates PMMR root derivation and single-leaf proofs against an independent
 * recursive naive reference implementation for all sizes N = 1..80.
 */
public final class RootReferenceTests {
    private RootReferenceTests() {}

    public static byte[] naiveRoot(int n) {
        if (n == 0) return PMMR.bagPeaks(0, new byte[0][]);
        List<byte[]> peaks = new ArrayList<>();
        int lo = 0, rem = n;
        while (rem > 0) {
            int len = Integer.highestOneBit(rem);
            peaks.add(perfect(lo, len));
            lo += len;
            rem -= len;
        }
        long size = 2L * n - Integer.bitCount(n);
        return PMMR.bagPeaks(size, peaks.toArray(new byte[0][]));
    }

    private static byte[] perfect(int lo, int len) {
        if (len == 1) return Hash.leaf(d(lo));
        return Hash.node(perfect(lo, len / 2), perfect(lo + len / 2, len / 2));
    }

    public static void run() {
        for (int n = 1; n <= 80; n++) {
            PMMR m = new PMMR();
            for (int i = 0; i < n; i++) m.append(d(i));
            check(Arrays.equals(m.root(), naiveRoot(n)), "root n=" + n);
            for (int i = 0; i < n; i++) {
                check(MerkleProof.verify(m.proof(i), d(i), m.root()), "proof n=" + n + " i=" + i);
            }
        }
        System.out.println("[ok] roots match reference, all proofs verify (n = 1..80)");
    }
}
