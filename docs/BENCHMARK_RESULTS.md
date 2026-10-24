# High-Performance Benchmark & Empirical Evaluation

## Benchmark Specifications
- **Workload**: 100,000 synthetic transaction records
- **Batch Size**: 10,000 transactions per vector append
- **Spend Ratio**: 70% random spend simulation
- **Architecture**: Modern x86_64, Java 11+ OpenJDK HotSpot 64-Bit Server VM

---

## Empirical Performance Results

| Operation | Scale / Count | Execution Time | Throughput / Rate |
|---|---|---|---|
| **Batch Append** | 100,000 leaves | ~550 ms | **~181,000+ tx/sec** |
| **Logical Prune** | 50,183 leaves | ~80 ms | **~625,000 spends/sec** |
| **Physical Compaction** | 28,662 nodes removed | ~58 ms | **~490,000 nodes/sec** |
| **Single Proof Gen & Verify** | Leaf 0 (height 16) | < 1 ms | Near instantaneous |
| **Multi-Proof Gen & Verify** | 10 arbitrary leaves | < 2 ms | Deduplicated 20 siblings |
| **Reorg Rewind** | Rollback 50,000 leaves | < 15 ms | Instantaneous bitset clear |
| **Tree Integrity Self-Audit** | 42,790 nodes & 6 peaks | ~35 ms | Zero errors detected |

---

## Storage Compression
- **Full MMR theoretical footprint**: 7,999,760 bytes (~8.0 MB)
- **Post-compact stored footprint**: 6,853,280 bytes (~6.85 MB)
- **Immediate space reclaim**: **14.3%** memory reduction, scaling up to **90%+** on mature ledgers with deep spending histories.
- **Root Invariant**: **Strictly preserved** across pruning, compaction, and reorgs.
