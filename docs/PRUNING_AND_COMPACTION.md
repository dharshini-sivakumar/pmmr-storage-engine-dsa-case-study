# Pruning Mechanics & Invariant-Preserving Compaction

## 1. Motivation
In append-only systems (such as UTXO-based cryptocurrencies like Bitcoin and Grin), transactions become spent over time.
Storing historical spent data in RAM or active storage indefinitely results in unbounded growth.
The PMMR allows physical removal of dead nodes while ensuring that **the root hash remains completely invariant** and **proofs for all remaining unspent leaves remain valid**.

---

## 2. Death Propagation Rule
A node $n$ is considered **dead** if:
1. $n$ is a leaf and the corresponding transaction has been spent, OR
2. Both of $n$'s direct children are dead.

When a leaf is marked spent in `PruningManager`, death propagates upward recursively until reaching a parent whose sibling is still alive (or reaching a peak).

---

## 3. Compaction Removability Invariant
A node $n$ is physically pruned from storage if and only if:
$$\text{removable}(n) \iff \text{dead}(n) \land \text{dead}(\text{parent}(n))$$

### Proof Witness Preservation:
If a node $n$ is dead, but its sibling is still live, its parent is also live.
Therefore, node $n$ **must be retained** in storage as a cryptographic witness so that the live sibling can generate an inclusion proof up to the peak!

### Peak Invariance:
Peaks are never removed, as they are required to compute the root commitment.

---

## 4. Complexity
- **Logical Pruning**: $O(\log n)$ bitset operations per spent leaf.
- **Physical Compaction**: $O(m)$ single-pass array compaction, where $m$ is the number of stored nodes.
