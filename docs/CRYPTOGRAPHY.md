# Cryptographic Specification & Security Properties

## 1. SHA-256 Domain Separation
Domain separation ensures that hashes across different structural levels can never collide, thwarting second-preimage attacks:

- **Leaf Hash**:
  $$H_{\text{leaf}}(\text{data}) = \text{SHA-256}(0\text{x}00 \mathbin{\Vert} \text{data})$$
- **Internal Node Hash**:
  $$H_{\text{node}}(\text{left}, \text{right}) = \text{SHA-256}(0\text{x}01 \mathbin{\Vert} \text{left} \mathbin{\Vert} \text{right})$$
- **Root Commitment**:
  $$H_{\text{root}}(\text{size}, \text{baggedPeaks}) = \text{SHA-256}(0\text{x}02 \mathbin{\Vert} \text{size}_{64\text{-bit BE}} \mathbin{\Vert} \text{baggedPeaks})$$

Binding the MMR node size into the root commitment guarantees protection against truncation and extension ambiguity.

---

## 2. Bagging Peaks
When an MMR contains multiple peaks $P_0, P_1, \dots, P_{k-1}$ (left-to-right, tallest-to-shortest):
1. Bagging accumulates the peaks from right to left:
   $$\text{acc}_{k-1} = P_{k-1}$$
   $$\text{acc}_{i} = H_{\text{node}}(P_i, \text{acc}_{i+1}) \quad \text{for } i = k-2 \dots 0$$
2. The final commitment is:
   $$\text{Root} = H_{\text{root}}(\text{size}, \text{acc}_0)$$

---

## 3. Preimage Resistance & Tamper Detection
Any alteration of transaction data, peak position, or intermediate sibling hashes results in an avalanche failure in SHA-256 verification, triggering immediate proof rejection.
Stateless proof verification recomputes left/right node orientation mathematically from leaf indices, preventing malicious provers from swapping sibling order.
