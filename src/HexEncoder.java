package pmmr;

/**
 * Utility for formatting and decoding hexadecimal byte representations.
 */
public final class HexEncoder {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private HexEncoder() {}

    /**
     * Encodes a byte array to lowercase hexadecimal string.
     */
    public static String toHex(byte[] bytes) {
        if (bytes == null) return "";
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX_CHARS[v >>> 4];
            out[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * Decodes a hexadecimal string into a byte array.
     */
    public static byte[] fromHex(String hex) {
        if (hex == null || hex.isEmpty()) return new byte[0];
        int len = hex.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Hex string must have an even length");
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
