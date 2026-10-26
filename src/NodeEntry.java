package pmmr;

import java.util.Arrays;

/**
 * Value object representing a stored MMR node entry (position and cryptographic hash).
 */
public final class NodeEntry {
    private final long position;
    private final byte[] hash;

    public NodeEntry(long position, byte[] hash) {
        if (position <= 0) {
            throw new IllegalArgumentException("Position must be positive (1-indexed): " + position);
        }
        if (hash == null || hash.length != Hash.SIZE) {
            throw new IllegalArgumentException("Hash must be exactly " + Hash.SIZE + " bytes");
        }
        this.position = position;
        this.hash = hash.clone();
    }

    public long getPosition() {
        return position;
    }

    public byte[] getHash() {
        return hash.clone();
    }

    public String getHexHash() {
        return Hash.hex(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeEntry)) return false;
        NodeEntry nodeEntry = (NodeEntry) o;
        return position == nodeEntry.position && Arrays.equals(hash, nodeEntry.hash);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(position);
        result = 31 * result + Arrays.hashCode(hash);
        return result;
    }

    @Override
    public String toString() {
        return "NodeEntry{pos=" + position + ", hash=" + getHexHash() + "}";
    }
}
