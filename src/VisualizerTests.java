package pmmr;

import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for ASCII Merkle Mountain Range visualization.
 */
public final class VisualizerTests {
    private VisualizerTests() {}

    public static void run() {
        PMMR vizTree = new PMMR();
        for (int i = 0; i < 7; i++) vizTree.append(d(i));
        vizTree.prune(1);
        String viz = vizTree.renderAsciiTree();
        check(viz.contains("Merkle Mountain Range") && viz.contains("Height") && viz.contains("DED"), "visualizer renders diagram");
        System.out.println("[ok] ascii mountain range visualizer");
    }
}
