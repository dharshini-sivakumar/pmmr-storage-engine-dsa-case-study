package pmmr;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for blockchain reorganization and rollback (rewind) functionality.
 */
public final class ReorgRewindTests {
    private ReorgRewindTests() {}

    public static void run() {
        PMMR reorg = new PMMR();
        for (int i = 0; i < 200; i++) reorg.append(d(i));
        PMMR checkpoint120 = new PMMR();
        for (int i = 0; i < 120; i++) checkpoint120.append(d(i));
        byte[] rootAt120 = checkpoint120.root();

        // Prune some leaves before and after 120
        reorg.prune(10);
        reorg.prune(150);
        check(reorg.prunedLeafCount() == 2, "2 leaves pruned before rewind");

        // Rewind back to 120
        reorg.rewind(120);
        check(reorg.leafCount() == 120, "rewind leaf count is 120");
        check(reorg.size() == MMRMath.sizeForLeafCount(120), "rewind size matches math");
        check(reorg.prunedLeafCount() == 1, "leaf 150 dead bit cleared on rewind");
        check(Arrays.equals(reorg.root(), rootAt120), "root after rewind matches exact state at 120");

        // Re-append alternate branch (fork simulation)
        for (int i = 120; i < 160; i++) {
            reorg.append(("alt-fork-" + i).getBytes(StandardCharsets.UTF_8));
        }
        check(reorg.leafCount() == 160, "fork extended to 160 leaves");
        AuditReport forkAudit = reorg.validateIntegrity();
        check(forkAudit.isValid(), "tree integrity valid after rewind and alternate fork");
        System.out.println("[ok] rewind / rollback");
    }
}
