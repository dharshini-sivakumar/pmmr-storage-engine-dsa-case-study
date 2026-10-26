# Cryptographic Proof System & Stateless Verification

The PMMR engine supports three classes of zero-knowledge / stateless proofs:

## 1. Single-Leaf Merkle Inclusion Proof (`MerkleProof`)
Proves that a transaction at a specific leaf index is included in the state represented by the root commitment.
- **Payload**:
  - `leafIndex`: 0-indexed leaf position
  - `mmrSize`: size of the MMR when the proof was constructed
  - `siblings`: list of sibling hashes along the Merkle path to the peak ($O(\log n)$)
  - `peaks`: array of all peak hashes in the MMR
- **Verification**:
  - Stateless: Requires only the proof, raw leaf data, and expected root hash.
  - Left/right node order is computed mathematically from the leaf index.

---

## 2. Aggregated Multi-Proof (`MultiProof`)
When a client (such as an SPV light wallet) needs to verify multiple leaves simultaneously, independent single-leaf proofs duplicate sibling hashes along shared ancestor subtrees.
- **Optimization**:
  `MultiProof` collects all unique target leaves and shares intermediate subtree computations. Sibling hashes are deduplicated into a single hash map.
- **Verification**:
  Stateless reconstruction of subtrees ascending simultaneously to the peaks.

---

## 3. Historical Consistency / Prefix Proof (`ConsistencyProof`)
Proves that an earlier MMR state at size $M$ (root $R_M$) is an immutable, untampered historical prefix of the current state at size $N$ (root $R_N$).
- **Verification**:
  1. Checks that old peaks bag to $R_M$.
  2. Checks that current peaks bag to $R_N$.
  3. Verifies that every historical peak either remains a peak at size $N$ or climbs deterministically along an authenticated Merkle path to an enclosing peak in size $N$.
