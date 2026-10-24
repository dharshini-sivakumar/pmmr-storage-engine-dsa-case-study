# Interactive CLI User Manual

The interactive terminal application provides comprehensive control over the PMMR storage engine.

```
======================= ADVANCED PMMR ENGINE =======================
 1. Append transaction           2. Batch append N transactions
 3. Prune (spend) leaf           4. Compact (reclaim memory)
 5. Show root & peaks            6. Prove & verify single leaf
 7. Prove & verify Multi-Proof   8. Consistency / Prefix Proof
 9. Rewind / Rollback (Reorg)   10. Self-Audit Tree Integrity
11. Render ASCII Mountains      12. Engine Statistics
13. Save to file                14. Load from file
15. Benchmark demo (100k)        0. Exit
====================================================================
```

### Options Guide:
- **1. Append transaction**: Appends a single utf-8 string, hashes with leaf domain prefix `0x00`, updates peaks.
- **2. Batch append N transactions**: Generates `tx-N` payloads in bulk to demonstrate vector insertion throughput.
- **3. Prune (spend) leaf**: Marks leaf index spent, propagating death bits up to peak level.
- **4. Compact (reclaim memory)**: Rewrites parallel position/hash storage arrays, purging dead nodes without live parents.
- **5. Show root & peaks**: Displays current root hex commitment and peak positions.
- **6. Prove & verify single leaf**: Generates inclusion proof and statelessly validates against trusted root.
- **7. Prove & verify Multi-Proof**: Aggregates comma-separated leaf indices into one deduplicated proof structure.
- **8. Consistency / Prefix Proof**: Verifies that state at an earlier size is an unperturbed prefix of current state.
- **9. Rewind / Rollback (Reorg)**: Simulates a blockchain reorganization, rolling back state to a previous leaf count.
- **10. Self-Audit Tree Integrity**: Sweeps entire store to verify parallel array ordering, child-parent hashes, and peaks.
- **11. Render ASCII Mountains**: Visualizes the binary mountains in terminal ASCII.
- **12. Engine Statistics**: Displays total nodes, leaves, unspent/pruned counts, payload bytes, and savings percentage.
- **13. Save to file**: Serializes state to binary file `ledger.pmmr`.
- **14. Load from file**: Restores state from `ledger.pmmr`.
- **15. Benchmark demo (100k)**: Runs the complete 100,000 transaction performance benchmark.
- **0. Exit**: Terminates the program.
