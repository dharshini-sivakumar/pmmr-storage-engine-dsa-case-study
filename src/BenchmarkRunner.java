package pmmr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Benchmark runner executing an automated 100,000 transaction workload demo.
 * Tests:
 * 1. High-throughput batch insertion (10k batches)
 * 2. Logical spending/pruning (70% of ledger)
 * 3. Invariant-preserving physical compaction
 * 4. Cryptographic root invariance validation
 * 5. Single-leaf Merkle proof verification
 * 6. Aggregated multi-leaf proof verification
 * 7. Blockchain reorganization / rewind simulation
 * 8. Full tree cryptographic integrity self-audit
 */
public final class BenchmarkRunner {
    private BenchmarkRunner() {}

    public static void runDemo(int n) {
        CLIFormatter.printBenchmarkHeader();

        PMMR engine = new PMMR();

        // 1. Appending
        System.out.println("[1] Appending " + n + " transactions in batches of 10,000...");
        long t0 = System.nanoTime();
        int batchSize = 10_000;
        for (int b = 0; b < n; b += batchSize) {
            List<byte[]> batch = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
                batch.add(("tx-" + (b + i)).getBytes(StandardCharsets.UTF_8));
            }
            engine.batchAppend(batch);
        }
        double appendMs = (System.nanoTime() - t0) / 1e6;
        System.out.printf("    Finished append: %d leaves in %.2f ms (%.0f tx/sec)%n",
                n, appendMs, (n / (appendMs / 1000.0)));
        byte[] originalRoot = engine.root();
        System.out.println("    Root Commitment : " + Hash.hex(originalRoot));
        System.out.println("    Pre-prune Stats : " + engine.stats());

        // Snapshot historical size for consistency proof later
        long checkpointSize = engine.size();
        byte[] checkpointRoot = originalRoot.clone();

        // 2. Spending / Pruning leaves
        System.out.println("\n[2] Simulating random transaction spends (~70% of ledger)...");
        Random rnd = new Random(42);
        int spendCount = (int) (n * 0.70);
        long tPrune = System.nanoTime();
        for (int i = 0; i < spendCount; i++) {
            engine.prune(rnd.nextInt(n));
        }
        double pruneMs = (System.nanoTime() - tPrune) / 1e6;
        System.out.printf("    Pruned %d leaves in %.2f ms%n", engine.prunedLeafCount(), pruneMs);
        System.out.println("    Post-prune Stats: " + engine.stats() + " (logical only)");

        // 3. Physical Compaction
        System.out.println("\n[3] Executing physical array compaction...");
        long tCompact = System.nanoTime();
        int removed = engine.compact();
        double compactMs = (System.nanoTime() - tCompact) / 1e6;
        System.out.printf("    Compacted %d nodes in %.2f ms%n", removed, compactMs);
        System.out.println("    Post-compact    : " + engine.stats());

        // 4. Verify Root Invariant
        boolean rootMatch = Arrays.equals(originalRoot, engine.root());
        System.out.println("\n[4] Cryptographic Root Invariant after compaction: " + (rootMatch ? "PASSED (ROOT UNCHANGED)" : "FAILED"));

        // 5. Single Leaf Proof Verification
        long unspent = 0;
        while (!engine.isUnspent(unspent)) unspent++;
        MerkleProof singleProof = engine.proof(unspent);
        boolean singleOk = MerkleProof.verify(singleProof, ("tx-" + unspent).getBytes(StandardCharsets.UTF_8), engine.root());
        System.out.printf("\n[5] Single-Leaf Proof for Leaf %d: %d siblings, Valid: %s%n",
                unspent, singleProof.siblings.size(), singleOk);

        // 6. Aggregated MultiProof
        List<Long> multiTargets = new ArrayList<>();
        for (long i = 0; i < n && multiTargets.size() < 10; i++) {
            if (engine.isUnspent(i)) multiTargets.add(i);
        }
        MultiProof mp = engine.multiProof(multiTargets);
        List<byte[]> mpData = new ArrayList<>();
        for (long idx : multiTargets) mpData.add(("tx-" + idx).getBytes(StandardCharsets.UTF_8));
        boolean multiOk = MultiProof.verify(mp, mpData, engine.root());
        System.out.printf("\n[6] Aggregated Multi-Proof for %d leaves: %d shared siblings. Valid: %s%n",
                multiTargets.size(), mp.proofHashes.size(), multiOk);

        // 7. Blockchain Reorg / Rewind Simulation
        System.out.println("\n[7] Simulating Blockchain Reorganization (Rewind to 50,000 leaves)...");
        engine.rewind(50_000);
        System.out.printf("    After rewind: %d leaves, size = %d nodes%n", engine.leafCount(), engine.size());

        // 8. Cryptographic Tree Integrity Audit
        System.out.println("\n[8] Running cryptographic self-audit post-reorg...");
        AuditReport audit = engine.validateIntegrity();
        System.out.printf("    Self-Audit Status: %s (Checked %d internal nodes, %d peaks)%n",
                audit.isValid() ? "ALL CHECKS PASSED" : "FAILED",
                audit.getCheckedInternalNodes(), audit.getCheckedPeaks());

        CLIFormatter.printBenchmarkFooter();
    }
}
