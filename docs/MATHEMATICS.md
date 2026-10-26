# Mathematical Foundations of Merkle Mountain Ranges

## 1. Post-Order Traversal & 1-Indexed Positions
An MMR lays out perfect binary trees linearly according to their post-order traversal (left child, right child, parent). 
Positions are **1-indexed**:

- Leaves sit at heights $h = 0$.
- A node has height $h$ if it represents a subtree of $2^{h+1} - 1$ total nodes.

### Height Calculation: $O(\log(\text{pos}))$
The height of any position $p$ is derived purely by subtracting the highest power of 2 sub-mountain:
```java
public static int height(long pos) {
    long p = pos;
    while ((p & (p + 1)) != 0) {
        p -= Long.highestOneBit(p) - 1;
    }
    return Long.bitCount(p) - 1;
}
```

---

## 2. Leaf Position Mapping: $O(1)$
For a 0-indexed leaf index $i$, its 1-indexed MMR position is given by:
$$\text{pos}(i) = 2i - \text{popcount}(i) + 1$$

And the total MMR size for $L$ leaves is:
$$\text{size}(L) = 2L - \text{popcount}(L)$$

---

## 3. Bidirectional Tree Navigation
- **Parent**:
  $$\text{parent}(p) = \begin{cases} p + 1 & \text{if } p \text{ is a right child} \\ p + 2^{h+1} & \text{if } p \text{ is a left child} \end{cases}$$
- **Sibling**:
  $$\text{sibling}(p) = \begin{cases} p - (2^{h+1} - 1) & \text{if } p \text{ is a right child} \\ p + (2^{h+1} - 1) & \text{if } p \text{ is a left child} \end{cases}$$
- **Children**:
  For an internal node at position $p$ with height $h$:
  $$\text{right}(p) = p - 1, \quad \text{left}(p) = p - 2^h$$

---

## 4. Peak Identification via Powers-of-Two Decomposition
Any integer $N$ can be decomposed into decreasing binary powers of 2. 
Each bit $2^k$ in the binary representation of the leaf count corresponds to a perfect binary tree of size $2^{k+1} - 1$.
The peaks are the roots of these respective mountains, ordered from tallest to shortest (left to right).
