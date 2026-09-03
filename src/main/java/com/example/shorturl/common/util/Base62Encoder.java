package com.example.shorturl.common.util;

/**
 * High-performance Base62 encoder/decoder using a pre-computed lookup table
 * for O(1) character-to-index mapping. Supports encoding non-negative longs
 * and decoding back with overflow protection.
 */
public final class Base62Encoder {

    private static final String BASE62_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = BASE62_CHARS.length(); // 62

    /**
     * Pre-computed decode map: ASCII char → index in BASE62_CHARS.
     * Filled with -1 for invalid characters, enabling O(1) lookup
     * instead of String.indexOf() which is O(n).
     */
    private static final int[] DECODE_MAP = new int[128];

    static {
        java.util.Arrays.fill(DECODE_MAP, -1);
        for (int i = 0; i < BASE62_CHARS.length(); i++) {
            DECODE_MAP[BASE62_CHARS.charAt(i)] = i;
        }
    }

    private Base62Encoder() {}

    /**
     * Encodes a non-negative long into a Base62 string.
     *
     * @param num the number to encode (must be >= 0)
     * @return Base62-encoded string
     * @throws IllegalArgumentException if num is negative
     */
    public static String encode(long num) {
        if (num < 0) {
            throw new IllegalArgumentException("Cannot encode negative number: " + num);
        }
        if (num == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int remainder = (int) (num % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string back to a long value.
     * Uses the pre-computed DECODE_MAP for O(1) per-character lookup
     * and checks for arithmetic overflow before each multiply-add.
     *
     * @param shortCode the Base62-encoded string
     * @return the decoded long value
     * @throws IllegalArgumentException if the input is null/empty, contains
     *         invalid characters, or the result overflows Long.MAX_VALUE
     */
    public static long decode(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            throw new IllegalArgumentException("Input must not be null or empty");
        }

        long num = 0;
        for (int i = 0; i < shortCode.length(); i++) {
            char c = shortCode.charAt(i);

            // Guard against non-ASCII or unmapped characters
            if (c >= 128 || DECODE_MAP[c] == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }

            int index = DECODE_MAP[c];

            // Overflow check: ensure num * BASE + index <= Long.MAX_VALUE
            if (num > (Long.MAX_VALUE - index) / BASE) {
                throw new IllegalArgumentException(
                        "Overflow: decoded value exceeds Long.MAX_VALUE for input: " + shortCode);
            }

            num = num * BASE + index;
        }
        return num;
    }
}
