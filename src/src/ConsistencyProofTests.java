package pmmr;

import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for historical consistency / range proofs verifying immutable prefix transitions.
 */
public final class ConsistencyProofTests {
    private ConsistencyProofTests() {}

    public static void run() {
        PMMR consTree = new PMMR();
        for (int i = 0; i < 30; i++) consTree.append(d(i));
        byte[] root30 = consTree.root();
        long size30 = consTree.size();

        for (int i = 30; i < 100; i++) consTree.append(d(i));
        byte[] root100 = consTree.root();

        ConsistencyProof cp = consTree.consistencyProof(size30);
        check(ConsistencyProof.verify(cp, root30, root100), "consistency proof from 30 to 100 verifies");

        // Tamper historical root
        byte[] badOldRoot = root30.clone();
        badOldRoot[0] ^= 1;
        check(!ConsistencyProof.verify(cp, badOldRoot, root100), "consistency proof rejects fake old root");

        // Tamper current root
        byte[] badCurRoot = root100.clone();
        badCurRoot[0] ^= 1;
        check(!ConsistencyProof.verify(cp, root30, badCurRoot), "consistency proof rejects fake current root");
        System.out.println("[ok] consistencyProof (historical prefix verification)");
    }
}
