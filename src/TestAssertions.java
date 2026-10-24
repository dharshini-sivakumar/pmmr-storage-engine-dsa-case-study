package pmmr;

import java.nio.charset.StandardCharsets;

/**
 * Common test assertions and utility functions for the PMMR test suite.
 */
public final class TestAssertions {
    private static int checks = 0;

    private TestAssertions() {}

    public static void check(boolean cond, String msg) {
        checks++;
        if (!cond) {
            System.err.println("FAIL: " + msg);
            System.exit(1);
        }
    }

    public static byte[] d(int i) {
        return ("tx-" + i).getBytes(StandardCharsets.UTF_8);
    }

    public static int getChecks() {
        return checks;
    }

    public static void resetChecks() {
        checks = 0;
    }
}
