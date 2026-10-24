package pmmr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Visualizer that generates formatted ASCII diagrams of a Merkle Mountain Range.
 * Renders node heights, peak markers (*), and status markers:
 *   LIV: Live node
 *   DED: Logically pruned node
 *   CMP: Physically compacted node
 */
public final class TreeVisualizer {
    private TreeVisualizer() {}

    public static String render(PMMR mmr, int maxLeaves) {
        long leaves = mmr.leafCount();
        if (leaves == 0) return "MMR is empty (size = 0)";
        if (leaves > maxLeaves) {
            return String.format("MMR too large to render visually (%d leaves > max %d). Use stats() instead.", leaves, maxLeaves);
        }

        long size = mmr.size();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Merkle Mountain Range (Leaves: %d, Nodes: %d) ===%n", leaves, size));
        long[] pks = MMRMath.peaks(size);
        Set<Long> peakSet = new HashSet<>();
        for (long p : pks) peakSet.add(p);

        sb.append("Peaks: ").append(Arrays.toString(pks)).append(" | Root: ").append(Hash.hex(mmr.root())).append("\n\n");

        int maxH = 0;
        for (long p : pks) {
            maxH = Math.max(maxH, MMRMath.height(p));
        }

        for (int h = maxH; h >= 0; h--) {
            sb.append(String.format("Height %2d: ", h));
            for (long p = 1; p <= size; p++) {
                if (MMRMath.height(p) == h) {
                    String marker = peakSet.contains(p) ? "*" : " ";
                    String status = !mmr.hasNode(p) ? "CMP" : (mmr.isDead(p) ? "DED" : "LIV");
                    sb.append(String.format("[%02d:%s%s] ", p, status, marker));
                }
            }
            sb.append("\n");
        }

        sb.append("\nLegend: [pos:status*] where * = Peak, LIV = Live, DED = Dead (Pruned), CMP = Compacted\n");
        return sb.toString();
    }
}
