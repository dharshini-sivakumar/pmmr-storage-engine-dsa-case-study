package pmmr;

import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for single-leaf inclusion proof verification and cryptographic tamper detection.
 */
public final class ProofTests {
    private ProofTests() {}

    public static void run() {
        PMMR t = new PMMR();
        for (int i = 0; i < 50; i++) t.append(d(i));
        MerkleProof p = t.proof(13);
        check(!MerkleProof.verify(p, d(14), t.root()), "wrong data rejected");
        byte[] badRoot = t.root().clone(); badRoot[0] ^= 1;
        check(!MerkleProof.verify(p, d(13), badRoot), "wrong root rejected");
        MerkleProof forged = new MerkleProof(14, p.mmrSize, p.siblings, p.peaks);
        check(!MerkleProof.verify(forged, d(13), t.root()), "wrong index rejected");
        System.out.println("[ok] tampering detected");
    }
}
