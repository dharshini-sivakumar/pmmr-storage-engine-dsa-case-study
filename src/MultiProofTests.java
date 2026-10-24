package pmmr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for aggregated multi-leaf Merkle proofs, deduplication, and tampering detection.
 */
public final class MultiProofTests {
    private MultiProofTests() {}

    public static void run() {
        PMMR mpTree = new PMMR();
        for (int i = 0; i < 64; i++) mpTree.append(d(i));
        byte[] mpRoot = mpTree.root();

        List<List<Long>> testSubsets = List.of(
            List.of(0L, 1L),
            List.of(0L, 31L, 63L),
            List.of(5L, 6L, 7L, 8L),
            List.of(2L, 4L, 8L, 16L, 32L),
            List.of(17L)
        );

        for (List<Long> subset : testSubsets) {
            MultiProof mp = mpTree.multiProof(subset);
            List<byte[]> leafDatas = new ArrayList<>();
            for (long idx : subset) leafDatas.add(d((int) idx));

            // Verify valid multi-proof
            check(MultiProof.verify(mp, leafDatas, mpRoot), "multiProof verifies for " + subset);

            // Tamper leaf data
            List<byte[]> badDatas = new ArrayList<>(leafDatas);
            badDatas.set(0, "tampered".getBytes(StandardCharsets.UTF_8));
            check(!MultiProof.verify(mp, badDatas, mpRoot), "multiProof rejects tampered data");

            // Tamper root
            byte[] badMpRoot = mpRoot.clone();
            badMpRoot[0] ^= 1;
            check(!MultiProof.verify(mp, leafDatas, badMpRoot), "multiProof rejects bad root");
        }
        System.out.println("[ok] multiProof generation, verification, and tampering");
    }
}
