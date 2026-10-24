package pmmr;

/**
 * Terminal UI formatter for the PMMR CLI application.
 */
public final class CLIFormatter {
    private CLIFormatter() {}

    public static void printBanner() {
        System.out.println("\n======================= ADVANCED PMMR ENGINE =======================");
        System.out.println(" 1. Append transaction           2. Batch append N transactions");
        System.out.println(" 3. Prune (spend) leaf           4. Compact (reclaim memory)");
        System.out.println(" 5. Show root & peaks            6. Prove & verify single leaf");
        System.out.println(" 7. Prove & verify Multi-Proof   8. Consistency / Prefix Proof");
        System.out.println(" 9. Rewind / Rollback (Reorg)   10. Self-Audit Tree Integrity");
        System.out.println("11. Render ASCII Mountains      12. Engine Statistics");
        System.out.println("13. Save to file                14. Load from file");
        System.out.println("15. Benchmark demo (100k)        0. Exit");
        System.out.println("====================================================================");
        System.out.print("Select an option > ");
    }

    public static void printBenchmarkHeader() {
        System.out.println("=================================================================");
        System.out.println("        PMMR STORAGE ENGINE - HIGH-PERFORMANCE BENCHMARK        ");
        System.out.println("=================================================================");
    }

    public static void printBenchmarkFooter() {
        System.out.println("\n=================================================================");
        System.out.println("                     BENCHMARK COMPLETE!                         ");
        System.out.println("=================================================================\n");
    }
}
