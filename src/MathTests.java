package pmmr;

import java.util.Arrays;
import static pmmr.TestAssertions.check;

/**
 * Unit tests for MMR position arithmetic and bitwise mathematics.
 */
public final class MathTests {
    private MathTests() {}

    public static void run() {
        int[] expH = {0, 0, 1, 0, 0, 1, 2, 0, 0, 1, 0};
        for (int i = 0; i < expH.length; i++) check(MMRMath.height(i + 1) == expH[i], "height " + (i + 1));
        long[] expLeaf = {1, 2, 4, 5, 8, 9, 11, 12, 16};
        for (int i = 0; i < expLeaf.length; i++) check(MMRMath.leafPos(i) == expLeaf[i], "leafPos " + i);
        for (int i = 0; i < expLeaf.length; i++) check(MMRMath.leafIndex(expLeaf[i]) == i, "leafIndex " + i);
        check(MMRMath.isLeaf(1) && MMRMath.isLeaf(2) && !MMRMath.isLeaf(3), "isLeaf check");
        check(Arrays.equals(MMRMath.children(3), new long[]{1, 2}), "children of 3");
        check(Arrays.equals(MMRMath.children(7), new long[]{3, 6}), "children of 7");
        check(Arrays.equals(MMRMath.peaks(10), new long[]{7, 10}), "peaks(10)");
        check(Arrays.equals(MMRMath.peaks(11), new long[]{7, 10, 11}), "peaks(11)");
        check(MMRMath.leafCount(11) == 7, "leafCount(11)");
        check(MMRMath.sizeForLeafCount(7) == 11, "sizeForLeafCount(7)");
        System.out.println("[ok] position math");
    }
}
