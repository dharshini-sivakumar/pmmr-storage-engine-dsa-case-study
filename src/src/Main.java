package pmmr;

import java.util.Scanner;

/**
 * Application entry point for the Advanced PMMR Storage Engine.
 * Supports interactive CLI mode and automated benchmark demonstrations.
 *
 * Usage:
 *   java pmmr.Main          (interactive mode)
 *   java pmmr.Main demo     (automated 100k benchmark demo)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("demo")) {
            demo(100_000);
            return;
        }
        menu();
    }

    public static void menu() {
        InteractiveCLI cli = new InteractiveCLI(new Scanner(System.in));
        cli.start();
    }

    public static void demo(int n) {
        BenchmarkRunner.runDemo(n);
    }
}
