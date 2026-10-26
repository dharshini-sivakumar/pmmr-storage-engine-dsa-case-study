package pmmr;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Factory for creating SHA-256 MessageDigest instances.
 */
public final class DigestFactory {
    public static final String ALGORITHM = "SHA-256";

    private DigestFactory() {}

    /**
     * Creates a new SHA-256 MessageDigest instance.
     *
     * @return MessageDigest instance
     */
    public static MessageDigest createSha256() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm unavailable: " + ALGORITHM, e);
        }
    }
}
