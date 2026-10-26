package pmmr;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/** 
 * SHA-256 helpers with domain separation (leaf / inner node / root) to prevent second-preimage tricks.
 */
public final class Hash {
    public static final int SIZE = 32;
    public static final byte LEAF = DomainSeparation.LEAF;
    public static final byte NODE = DomainSeparation.NODE;
    public static final byte ROOT = DomainSeparation.ROOT;

    private Hash() {}

    private static MessageDigest md() {
        return DigestFactory.createSha256();
    }

    /** H(0x00 || data) */
    public static byte[] leaf(byte[] data) {
        MessageDigest d = md();
        d.update(LEAF);
        return d.digest(data);
    }

    /** H(0x01 || left || right) */
    public static byte[] node(byte[] left, byte[] right) {
        MessageDigest d = md();
        d.update(NODE);
        d.update(left);
        return d.digest(right);
    }

    /** H(0x02 || mmrSize || baggedPeaks) - binds the root to the MMR size. */
    public static byte[] root(long mmrSize, byte[] bagged) {
        MessageDigest d = md();
        d.update(ROOT);
        d.update(ByteBuffer.allocate(8).putLong(mmrSize).array());
        return d.digest(bagged);
    }

    public static String hex(byte[] b) {
        return HexEncoder.toHex(b);
    }
}
