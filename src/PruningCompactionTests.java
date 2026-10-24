package pmmr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for pruning and compaction preserving roots and unspent proofs,
 * double pruning, bad index, and appending after full compaction.
 */
public final class PruningCompactionTests {
    private PruningCompactionTests() {}

    public static void run() {
        // 1. pruning + compaction keep root & proofs
        Random rnd = new Random(7);
        for (int n : new int[]{1, 2, 3, 7, 8, 31, 100, 1000}) {
            PMMR m = new PMMR();
            for (int i = 0; i < n; i++) m.append(d(i));
            byte[] root = m.root();
            Set<Integer> spent = new HashSet<>();
            for (int i = 0; i < n; i++) {
                if (rnd.nextInt(10) < 8) {
                    m.prune(i);
                    spent.add(i);
                }
            }
            check(Arrays.equals(root, m.root()), "root after prune n=" + n);
            int before = m.storedNodes();
            m.compact();
            check(m.storedNodes() <= before, "compact shrinks n=" + n);
            check(Arrays.equals(root, m.root()), "root after compact n=" + n);
            for (int i = 0; i < n; i++) {
                if (spent.contains(i)) {
                    boolean threw = false;
                    try {
                        m.proof(i);
                    } catch (IllegalArgumentException e) {
                        threw = true;
                    }
                    check(threw, "proof of spent leaf rejected");
                } else {
                    check(MerkleProof.verify(m.proof(i), d(i), root), "proof after compact n=" + n + " i=" + i);
                }
            }
        }
        System.out.println("[ok] prune + compact preserve root and unspent proofs");

        // 2. fully pruned, then keep appending after compaction
        PMMR a = new PMMR(), ref = new PMMR();
        for (int i = 0; i < 64; i++) {
            a.append(d(i));
            ref.append(d(i));
        }
        for (int i = 0; i < 64; i++) a.prune(i);
        a.compact();
        check(a.storedNodes() == 1, "fully pruned MMR keeps only its peak");
        for (int i = 64; i < 200; i++) {
            a.append(d(i));
            ref.append(d(i));
        }
        check(Arrays.equals(a.root(), ref.root()), "append after compaction");
        check(MerkleProof.verify(a.proof(150), d(150), ref.root()), "proof after append-after-compact");
        System.out.println("[ok] append after compaction");

        // 3. double prune + bad index
        PMMR e = new PMMR();
        e.append(d(0));
        check(e.prune(0) && !e.prune(0), "double prune");
        boolean threw = false;
        try {
            e.prune(5);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check(threw, "bad index");
        System.out.println("[ok] edge cases");
    }
}
