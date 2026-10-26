package pmmr;

/**
 * Domain separation tags for SHA-256 Merkle tree hashing.
 * Domain separation guarantees that leaf hashes cannot collide with internal node hashes,
 * nor can node hashes collide with root commitments (preimage resistance).
 */
public final class DomainSeparation {
    public static final byte LEAF = 0x00;
    public static final byte NODE = 0x01;
    public static final byte ROOT = 0x02;

    private DomainSeparation() {}
}
