package pmmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Diagnostic audit report produced by PMMR.validateIntegrity().
 * Verifies cryptographic consistency, peak validity, sibling hash linkages, and dead bit invariants.
 */
public final class AuditReport {
    private final boolean valid;
    private final long totalSize;
    private final int storedNodes;
    private final int checkedInternalNodes;
    private final int checkedPeaks;
    private final List<String> errors;

    public AuditReport(boolean valid, long totalSize, int storedNodes,
                       int checkedInternalNodes, int checkedPeaks, List<String> errors) {
        this.valid = valid;
        this.totalSize = totalSize;
        this.storedNodes = storedNodes;
        this.checkedInternalNodes = checkedInternalNodes;
        this.checkedPeaks = checkedPeaks;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public boolean isValid() { return valid; }
    public long getTotalSize() { return totalSize; }
    public int getStoredNodes() { return storedNodes; }
    public int getCheckedInternalNodes() { return checkedInternalNodes; }
    public int getCheckedPeaks() { return checkedPeaks; }
    public List<String> getErrors() { return errors; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("AuditReport[valid=%s, size=%d, stored=%d, internalChecked=%d, peaksChecked=%d]",
                valid, totalSize, storedNodes, checkedInternalNodes, checkedPeaks));
        if (!valid) {
            sb.append("\nErrors found:\n");
            for (String err : errors) {
                sb.append(" - ").append(err).append("\n");
            }
        }
        return sb.toString();
    }
}
