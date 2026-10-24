package pmmr;

/** 
 * Master test suite runner executing all modular unit and stress tests.
 * Usage: java pmmr.Tests 
 */
public class Tests {

    public static void main(String[] args) throws Exception {
        MathTests.run();
        RootReferenceTests.run();
        ProofTests.run();
        PruningCompactionTests.run();
        PersistenceTests.run();
        BatchAppendTests.run();
        ReorgRewindTests.run();
        MultiProofTests.run();
        ConsistencyProofTests.run();
        AuditTests.run();
        ConcurrencyTests.run();
        VisualizerTests.run();

        System.out.println("\n==============================================");
        System.out.println("ALL ADVANCED FEATURES AND CORE TESTS PASSED!");
        System.out.println("Total validation checks: " + TestAssertions.getChecks());
        System.out.println("==============================================");
    }
}
