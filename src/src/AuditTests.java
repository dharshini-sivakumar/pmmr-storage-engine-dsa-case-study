package pmmr;

import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for cryptographic tree integrity self-audit.
 */
public final class AuditTests {
    private AuditTests() {}

    public static void run() {
        PMMR auditTree = new PMMR();
        for (int i = 0; i < 100; i++) auditTree.append(d(i));
        AuditReport rep1 = auditTree.validateIntegrity();
        check(rep1.isValid() && rep1.getErrors().isEmpty(), "audit on full tree is clean");

        // Prune and compact, then audit
        for (int i = 0; i < 50; i += 2) auditTree.prune(i);
        auditTree.compact();
        AuditReport rep2 = auditTree.validateIntegrity();
        check(rep2.isValid() && rep2.getErrors().isEmpty(), "audit after prune and compact is clean");
        System.out.println("[ok] cryptographic integrity self-audit");
    }
}
