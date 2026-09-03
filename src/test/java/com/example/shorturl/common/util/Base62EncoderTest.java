package com.example.shorturl.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Nested
    @DisplayName("encode()")
    class Encode {

        @Test
        @DisplayName("should encode 0 to 'a'")
        void encodeZero() {
            assertEquals("a", Base62Encoder.encode(0));
        }

        @Test
        @DisplayName("should encode small numbers correctly")
        void encodeSmallNumbers() {
            assertEquals("b", Base62Encoder.encode(1));
            assertEquals("ba", Base62Encoder.encode(62));
            assertEquals("bb", Base62Encoder.encode(63));
        }

        @Test
        @DisplayName("should encode large numbers")
        void encodeLargeNumber() {
            String result = Base62Encoder.encode(1_000_000L);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            // Verify round-trip
            assertEquals(1_000_000L, Base62Encoder.decode(result));
        }

        @Test
        @DisplayName("should throw for negative numbers")
        void encodeNegative() {
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.encode(-1));
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.encode(Long.MIN_VALUE));
        }

        @Test
        @DisplayName("should encode Long.MAX_VALUE without error")
        void encodeLongMax() {
            String result = Base62Encoder.encode(Long.MAX_VALUE);
            assertNotNull(result);
            assertEquals(Long.MAX_VALUE, Base62Encoder.decode(result));
        }
    }

    @Nested
    @DisplayName("decode()")
    class Decode {

        @Test
        @DisplayName("should decode 'a' to 0")
        void decodeZero() {
            assertEquals(0, Base62Encoder.decode("a"));
        }

        @Test
        @DisplayName("should round-trip encode/decode")
        void roundTrip() {
            long[] values = {0, 1, 61, 62, 100, 999, 123456789L, Integer.MAX_VALUE};
            for (long v : values) {
                assertEquals(v, Base62Encoder.decode(Base62Encoder.encode(v)),
                        "Round-trip failed for: " + v);
            }
        }

        @Test
        @DisplayName("should throw for null input")
        void decodeNull() {
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode(null));
        }

        @Test
        @DisplayName("should throw for empty input")
        void decodeEmpty() {
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode(""));
        }

        @Test
        @DisplayName("should throw for invalid characters")
        void decodeInvalidChar() {
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("abc!"));
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("abc def"));
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode("$$$"));
        }

        @Test
        @DisplayName("should throw on overflow for extremely long input")
        void decodeOverflow() {
            // A string of all '9' chars (index 61) that would overflow long
            String hugeInput = "9".repeat(20);
            assertThrows(IllegalArgumentException.class, () -> Base62Encoder.decode(hugeInput));
        }
    }
}
