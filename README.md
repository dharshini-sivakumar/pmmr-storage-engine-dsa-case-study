Advanced PMMR Storage Engine - Pruned Merkle Mountain Range (Java)
DSA Lab Evaluation 3 - Data Structure: Merkle Tree (Merkle Mountain Range variant)
Problem Statement
Cryptographic ledgers grow continuously, raising storage and verification costs. A PMMR-based
storage engine is needed to prune and compact data while keeping verification fast, secure, and verifiable.
Solution in One Paragraph
Transactions are leaves of an append-only Merkle Mountain Range (MMR) — a sequence of perfect Merkle
trees ("mountains") whose roots ("peaks") are hashed together into one root commitment. When a
leaf is spent, it is pruned: a node becomes dead when its leaf is spent (or both children are dead),
and `compact()` physically deletes every dead node whose parent is also dead. Only live nodes, the
sibling hashes needed for proofs, and the peaks are kept, so the root never changes and proofs for
all unspent leaves stay valid.
---
Project Layout
```
src/pmmr/
  ├── Core Engine & Storage
  │   ├── PMMR.java                   Thread-safe engine coordinating storage, compaction, proofs
  │   ├── INodeStore.java             Storage interface defining append, get, compact, truncate
  │   ├── NodeStore.java              Array-based contiguous storage with binary search lookup
  │   ├── NodeEntry.java              Immutable (position, hash) value object
  │   ├── PruningManager.java         Bitset spend-tracking, death propagation, and removability
  │   ├── StorageMetrics.java         Storage metrics, byte tallies, and percentage compression
  │   └── PMMRSerializer.java         High-performance binary streaming persistence
  ├── MMR Mathematics
  │   ├── MMRMath.java                Unified mathematics facade (100% backward compatible)
  │   ├── MMRNavigator.java           Node height, parent, sibling, left/right child navigation
  │   ├── PeakCalculator.java         Peaks identification via binary powers-of-two decomposition
  │   └── LeafMapper.java             O(1) leaf-to-position mapping and leaf counting
  ├── Cryptography
  │   ├── Hash.java                   SHA-256 domain-separated hashing facade
  │   ├── DomainSeparation.java       Cryptographic domain separation constants (0x00, 0x01, 0x02)
  │   ├── DigestFactory.java          MessageDigest SHA-256 factory
  │   └── HexEncoder.java             Hexadecimal encoding and decoding utilities
  ├── Proof System
  │   ├── Proof.java                  Base marker interface for cryptographic proofs
  │   ├── MerkleProof.java            Stateless single-leaf inclusion proof
  │   ├── MultiProof.java             Aggregated multi-leaf proof with sibling deduplication
  │   ├── ConsistencyProof.java       Historical range proof verifying immutable prefix history
  │   └── ProofVerifier.java          Unified verification coordinator
  ├── Diagnostics & Visualization
  │   ├── AuditReport.java            Cryptographic self-audit diagnostics report
  │   ├── TreeIntegrityAuditor.java   Full-tree structural and hash audit engine
  │   └── TreeVisualizer.java         Terminal ASCII mountain range visualizer
  ├── Application & CLI
  │   ├── Main.java                   Main entry point
  │   ├── InteractiveCLI.java         Interactive terminal menu controller
  │   ├── BenchmarkRunner.java        100,000-tx automated benchmark workload runner
  │   └── CLIFormatter.java           Terminal banners, menus, and text formatters
  └── Modular Test Suites
      ├── Tests.java                  Master test runner executing all suites
      ├── TestAssertions.java         Assertion framework and validation check counter
      ├── MathTests.java              MMR arithmetic and navigation unit tests
      ├── RootReferenceTests.java     Roots validated against naive recursive reference (n=1..80)
      ├── ProofTests.java             Single-leaf inclusion and tampering tests
      ├── PruningCompactionTests.java Pruning, death propagation, invariant retention tests
      ├── PersistenceTests.java       Binary save/load serialization tests
      ├── BatchAppendTests.java       Batch insertion throughput and consistency tests
      ├── ReorgRewindTests.java       Blockchain reorg rollback and alternate fork tests
      ├── MultiProofTests.java        Aggregated multi-proof generation & verification tests
      ├── ConsistencyProofTests.java  Historical prefix consistency range tests
      ├── AuditTests.java             Cryptographic self-audit diagnostic tests
      ├── ConcurrencyTests.java       Multi-threaded read/write concurrency stress tests
      └── VisualizerTests.java        ASCII tree diagram rendering tests

docs/                                 Technical documentation & specifications
data/                                 Sample transaction ledgers & reference test vectors
scripts/                              Automated build, test, demo, and clean scripts
.github/workflows/                    GitHub Actions continuous integration workflows
pom.xml & build.gradle                Maven & Gradle build configuration descriptors
run.sh / run.bat                      Convenience build and execution helpers
```
---
Key Features & Advanced Capabilities
Pure Bitwise MMR Mathematics (`MMRMath.java`):
Zero pointers or tree node objects; 1-indexed post-order traversal arithmetic.
$O(\log n)$ height calculation, $O(1)$ leaf-to-position mapping: $\text{pos} = 2i - \text{popcount}(i) + 1$.
Mathematical peak identification via binary powers-of-two decomposition.
Bidirectional navigation: parent, sibling, left/right children, and inverse leaf index lookup.
Domain-Separated Cryptography (`Hash.java`):
Leaf hashing: $H(0\text{x}00 \mathbin{\Vert} \text{data})$
Node hashing: $H(0\text{x}01 \mathbin{\Vert} \text{left} \mathbin{\Vert} \text{right})$
Root commitment: $H(0\text{x}02 \mathbin{\Vert} \text{mmrSize} \mathbin{\Vert} \text{baggedPeaks})$
Prevents second-preimage attacks and protects against length-extension/truncation attacks.
High-Performance Batch Append (`PMMR.batchAppend`):
Bulk vector insertion achieving ~350,000+ tx/sec throughput.
Amortized $O(1)$ per leaf insertion.
Cryptographic Pruning & Invariant-Preserving Compaction:
Logical pruning using `java.util.BitSet` (1 bit per position).
Node death propagation rule: $\text{dead}(n) = \text{leaf_spent} \lor (\text{dead}(\text{left}) \land \text{dead}(\text{right}))$.
Compaction retention rule: node $n$ is pruned if and only if $\text{dead}(n) \land \text{dead}(\text{parent}(n))$. Dead nodes with a live parent are retained as proof witnesses.
Peaks are always retained for root computation.
Root is strictly invariant across pruning and compaction.
Stateless Single & Multi-Leaf Proofs (`MerkleProof`, `MultiProof`):
Single-leaf inclusion proof: $O(\log n)$ size.
Aggregated Multi-Proof: Proves multiple leaves simultaneously by deduplicating common ancestor sibling hashes, drastically reducing proof payload size for SPV / light clients.
Historical Consistency / Range Proofs (`ConsistencyProof`):
Proves that an earlier state at size $M$ (root $R_M$) is an exact immutable prefix of state at size $N$ (root $R_N$).
Verifies that past transactions have never been tampered with or rewritten.
Blockchain Reorganization & Rewind (`PMMR.rewind`):
Supports rolling back unconfirmed/orphaned blocks to any historical leaf count.
Truncates stores, clears dead bits, re-tallies pruned counts, and enables alternate fork appending.
Cryptographic Integrity Self-Audit (`PMMR.validateIntegrity`):
Comprehensive diagnostic sweep checking parallel array order, re-verifying internal hashes from children, checking peak presence, and verifying root derivation.
Thread-Safe Multi-Reader Concurrency:
`ReentrantReadWriteLock` allows concurrent high-throughput proof verification and queries alongside synchronized writes.
ASCII Mountain Range Visualizer (`PMMR.renderAsciiTree`):
Direct terminal visualization of perfect binary trees, peaks, live, dead, and compacted nodes.
---
How to Run (JDK 11+)
```bash
# Run all tests (5,180+ checks)
./run.sh test     # Linux / macOS
run.bat test      # Windows

# Run benchmark demo (100,000 transactions)
./run.sh demo     # Linux / macOS
run.bat demo      # Windows

# Interactive CLI Menu
./run.sh          # Linux / macOS
run.bat           # Windows
```
---
Benchmark Results (100,000 Transactions)
Append: 100,000 transactions in 284 ms (~351,600 tx/sec).
Pruning: 50,183 leaves spent in 96 ms.
Compaction: 28,662 dead nodes removed in 19.8 ms (~14.3% storage reclaimed).
Root Invariant: `PASSED` (exact match before and after compaction).
Single-Leaf Proof: 16 siblings, verified `true`.
Multi-Leaf Proof (10 leaves): 20 shared siblings (vs 160 separate siblings), verified `true`.
Rewind: Instantaneous rollback to 50,000 leaves.
Integrity Self-Audit: `ALL CHECKS PASSED` across 42,790 internal nodes and 6 peaks.
---
Complexity Summary
Operation	Time Complexity	Space Complexity	Justification
`height(pos)`	$O(\log \text{pos})$	$O(1)$	Clears highest set bit per step
`leafPos(i)`	$O(1)$	$O(1)$	$2i - \text{popcount}(i) + 1$
`append(tx)`	Amortized $O(1)$, worst $O(\log n)$	$O(1)$	Merges = trailing 1-bits; binary counter amortized bound
`batchAppend(k)`	Amortized $O(k)$	$O(k)$	Vectorized appends
`prune(leaf)`	$O(\log n)$	$O(1)$	Climbs at most tree height
`compact()`	$O(m)$	$O(m)$	Single pass over node arrays
`rewind(k)`	$O(\log m)$	$O(1)$	Binary search truncation and bitset clear
`singleProof()`	$O(\log^2 n)$	$O(\log n)$	$O(\log n)$ tree height $\times$ binary search
`multiProof(k)`	$O(k \log n)$	$O(k \log n)$	Deduplicated subtree traversal
`consistencyProof`	$O(\log^2 n)$	$O(\log n)$	Old peak traversal to new peaks
`validateIntegrity`	$O(m \log m)$	$O(m)$	Full cryptographic self-check
