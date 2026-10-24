# System Architecture & Design Specification

## Overview
The **Pruned Merkle Mountain Range (PMMR)** Storage Engine is a high-performance cryptographic data structure designed for append-only distributed ledgers, blockchain state commitments, and verifiable event logs.

Unlike traditional binary Merkle trees that require power-of-two padding or re-hashing of the entire tree upon appends, the Merkle Mountain Range (MMR) is an append-only sequence of perfect binary trees.

```
       Height 2                     [07]
                                   /    \
       Height 1             [03]            [06]             [10]
                           /    \          /    \           /    \
       Height 0          [01]  [02]      [04]  [05]       [08]  [09]      [11] ...
```

---

## Component Architecture

The codebase is organized into modular single-responsibility components:

| Component | Class / Interface | Responsibility |
|---|---|---|
| **Core Engine** | `PMMR` | Thread-safe facade managing lifecycle, operations, and locking |
| **Storage Layer** | `INodeStore`, `NodeStore`, `NodeEntry` | Array-based contiguous storage with binary search lookup |
| **Pruning Engine** | `PruningManager` | Bitset spend-tracking, death propagation, and removability invariants |
| **Position Math** | `MMRMath`, `MMRNavigator`, `PeakCalculator`, `LeafMapper` | Zero-pointer 1-indexed post-order traversal arithmetic |
| **Cryptography** | `Hash`, `DomainSeparation`, `DigestFactory`, `HexEncoder` | Domain-separated SHA-256 hashing |
| **Proofs** | `Proof`, `MerkleProof`, `MultiProof`, `ConsistencyProof`, `ProofVerifier` | Inclusion, aggregation, and historical consistency proofs |
| **Diagnostics** | `AuditReport`, `TreeIntegrityAuditor` | Full tree cryptographic structural verification |
| **Visualization** | `TreeVisualizer` | Terminal ASCII tree rendering |
| **Metrics** | `StorageMetrics` | Payload bytes, theoretical bytes, and savings percentage |
| **Persistence** | `PMMRSerializer` | Binary streaming persistence with magic header verification |
| **CLI & Demo** | `Main`, `InteractiveCLI`, `BenchmarkRunner`, `CLIFormatter` | Command line application and automated 100k-tx benchmark |

---

## Concurrency & Thread-Safety

Thread safety is achieved using `java.util.concurrent.locks.ReentrantReadWriteLock`:
- **Read Lock (`readLock`)**: Multiple readers can concurrently compute roots, generate Merkle proofs, run multi-proof verifications, inspect storage metrics, and visualize trees without contention.
- **Write Lock (`writeLock`)**: Exclusive access is acquired during leaf appends, batch appends, pruning bit updates, physical compaction, and historical rewinds.
