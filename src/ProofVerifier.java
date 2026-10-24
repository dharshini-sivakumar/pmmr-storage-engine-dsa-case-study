package pmmr;

import java.util.List;

/**
 * Unified verification engine for all PMMR cryptographic proofs.
 * Delegates to stateless verification algorithms for MerkleProof, MultiProof, and ConsistencyProof.
 */
public final class ProofVerifier {
    private ProofVerifier() {}

    /**
     * Verifies a single-leaf inclusion proof against expected root.
     */
    public static boolean verifySingle(MerkleProof proof, byte[] leafData, byte[] expectedRoot) {
        return MerkleProof.verify(proof, leafData, expectedRoot);
    }

    /**
     * Verifies an aggregated multi-leaf proof against expected root.
     */
    public static boolean verifyMulti(MultiProof proof, List<byte[]> leafDataList, byte[] expectedRoot) {
        return MultiProof.verify(proof, leafDataList, expectedRoot);
    }

    /**
     * Verifies an historical prefix consistency proof between two MMR states.
     */
    public static boolean verifyConsistency(ConsistencyProof proof, byte[] expectedFromRoot, byte[] expectedToRoot) {
        return ConsistencyProof.verify(proof, expectedFromRoot, expectedToRoot);
    }
}
