package pmmr;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Interactive Command Line Interface controller for the PMMR Storage Engine.
 */
public final class InteractiveCLI {
    private final List<String> ledger = new ArrayList<>();
    private PMMR m = new PMMR();
    private final Scanner sc;

    public InteractiveCLI(Scanner scanner) {
        this.sc = scanner;
    }

    public void start() {
        // Prepopulate with a few initial items for quick exploration
        for (int i = 0; i < 7; i++) {
            String d = "tx-" + i;
            ledger.add(d);
            m.append(d);
        }

        while (true) {
            CLIFormatter.printBanner();

            if (!sc.hasNextLine()) return;
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1": {
                        System.out.print("Transaction data: ");
                        String d = sc.nextLine();
                        ledger.add(d);
                        long idx = m.append(d);
                        System.out.printf("Appended at leaf index: %d (MMR size: %d nodes)%n", idx, m.size());
                        break;
                    }
                    case "2": {
                        System.out.print("How many transactions to batch append: ");
                        int n = Integer.parseInt(sc.nextLine().trim());
                        List<byte[]> batch = new ArrayList<>(n);
                        int start = ledger.size();
                        for (int i = 0; i < n; i++) {
                            String d = "tx-" + (start + i);
                            ledger.add(d);
                            batch.add(d.getBytes(StandardCharsets.UTF_8));
                        }
                        long t0 = System.nanoTime();
                        long[] indices = m.batchAppend(batch);
                        double ms = (System.nanoTime() - t0) / 1e6;
                        System.out.printf("Batch appended %d txs in %.2f ms (leaves %d to %d)%n",
                                n, ms, indices[0], indices[indices.length - 1]);
                        break;
                    }
                    case "3": {
                        System.out.print("Leaf index to prune (spend): ");
                        long idx = Long.parseLong(sc.nextLine().trim());
                        boolean ok = m.prune(idx);
                        System.out.println(ok ? "Leaf " + idx + " marked DEAD (pruned)." : "Leaf " + idx + " was already pruned.");
                        break;
                    }
                    case "4": {
                        long t0 = System.nanoTime();
                        int removed = m.compact();
                        double ms = (System.nanoTime() - t0) / 1e6;
                        System.out.printf("Compacted: physically removed %d dead nodes in %.2f ms.%n", removed, ms);
                        System.out.println("New state: " + m.stats());
                        break;
                    }
                    case "5": {
                        System.out.println("Root commitment: " + Hash.hex(m.root()));
                        System.out.println("Peak positions : " + Arrays.toString(MMRMath.peaks(m.size())));
                        break;
                    }
                    case "6": {
                        System.out.print("Leaf index to prove: ");
                        long idx = Long.parseLong(sc.nextLine().trim());
                        if (idx < 0 || idx >= ledger.size()) {
                            System.out.println("Index out of tracked ledger bounds.");
                            break;
                        }
                        MerkleProof p = m.proof(idx);
                        byte[] data = ledger.get((int) idx).getBytes(StandardCharsets.UTF_8);
                        boolean ok = MerkleProof.verify(p, data, m.root());
                        System.out.printf("Single Leaf Proof: %d siblings, Stateless Verification: %s%n",
                                p.siblings.size(), ok ? "VALID" : "INVALID");
                        break;
                    }
                    case "7": {
                        System.out.print("Enter comma-separated leaf indices to batch-prove (e.g. 0,2,4): ");
                        String[] parts = sc.nextLine().trim().split(",");
                        List<Long> indices = new ArrayList<>();
                        List<byte[]> dataList = new ArrayList<>();
                        for (String part : parts) {
                            String s = part.trim();
                            if (!s.isEmpty()) {
                                long idx = Long.parseLong(s);
                                indices.add(idx);
                                dataList.add(ledger.get((int) idx).getBytes(StandardCharsets.UTF_8));
                            }
                        }
                        MultiProof mp = m.multiProof(indices);
                        boolean ok = MultiProof.verify(mp, dataList, m.root());
                        System.out.printf("Multi-Proof for %d leaves: %d deduplicated sibling hashes, %d peaks. Valid: %s%n",
                                indices.size(), mp.proofHashes.size(), mp.peakHashes.size(), ok ? "VALID" : "INVALID");
                        break;
                    }
                    case "8": {
                        System.out.print("Enter historical MMR size to verify prefix consistency: ");
                        long fromSize = Long.parseLong(sc.nextLine().trim());
                        ConsistencyProof cp = m.consistencyProof(fromSize);
                        byte[] oldRoot = PMMR.bagPeaks(fromSize, cp.oldPeaks);
                        boolean ok = ConsistencyProof.verify(cp, oldRoot, m.root());
                        System.out.printf("Historical Consistency Proof (size %d -> %d): Valid = %s%n",
                                fromSize, m.size(), ok ? "VERIFIED IMMUTABLE PREFIX" : "FAILED");
                        break;
                    }
                    case "9": {
                        System.out.print("Target leaf count to rewind / rollback to: ");
                        long targetLeaves = Long.parseLong(sc.nextLine().trim());
                        boolean ok = m.rewind(targetLeaves);
                        while (ledger.size() > targetLeaves) {
                            ledger.remove(ledger.size() - 1);
                        }
                        System.out.printf("Rewind result: %s. Current leaf count: %d, size: %d%n",
                                ok ? "SUCCESS" : "FAILED", m.leafCount(), m.size());
                        break;
                    }
                    case "10": {
                        System.out.println("Running comprehensive cryptographic tree self-audit...");
                        AuditReport report = m.validateIntegrity();
                        System.out.println(report);
                        break;
                    }
                    case "11": {
                        System.out.println(m.renderAsciiTree(32));
                        break;
                    }
                    case "12": {
                        System.out.println(m.stats());
                        break;
                    }
                    case "13": {
                        Path file = Path.of("ledger.pmmr");
                        m.save(file);
                        System.out.println("Saved binary state to " + file.toAbsolutePath());
                        break;
                    }
                    case "14": {
                        Path file = Path.of("ledger.pmmr");
                        m = PMMR.load(file);
                        System.out.println("Loaded binary state. " + m.stats());
                        break;
                    }
                    case "15": {
                        BenchmarkRunner.runDemo(100_000);
                        break;
                    }
                    case "0":
                        System.out.println("Exiting PMMR Storage Engine.");
                        return;
                    default:
                        System.out.println("Unknown option: " + choice);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
