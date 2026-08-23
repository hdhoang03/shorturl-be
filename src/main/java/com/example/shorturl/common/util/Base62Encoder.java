package com.example.shorturl.common.util;

public final class Base62Encoder {
    private static final String BASE62_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = BASE62_CHARS.length();

    private Base62Encoder() {}

    public static String encode(long num) {
        if (num <= 0) {
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

    public static long decode(String shortCode) {
        long num = 0;
        for (int i = 0; i < shortCode.length(); i++) {
            char c = shortCode.charAt(i);
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            num = num * BASE + index;
        }
        return num;
    }
}
