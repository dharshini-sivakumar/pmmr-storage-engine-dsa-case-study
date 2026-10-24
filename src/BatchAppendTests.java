package pmmr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for high-performance batch appending in comparison to sequential appends.
 */
public final class BatchAppendTests {
    private BatchAppendTests() {}

    public static void run() {
        PMMR seq = new PMMR(), bat = new PMMR();
        List<byte[]> batchItems = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            byte[] item = d(i);
            seq.append(item);
            batchItems.add(item);
        }
        long[] indices = bat.batchAppend(batchItems);
        check(Arrays.equals(seq.root(), bat.root()), "batch append root matches sequential");
        check(bat.leafCount() == 150, "batch leaf count = 150");
        check(indices.length == 150 && indices[149] == 149, "batch indices correct");
        System.out.println("[ok] batch append");
    }
}
