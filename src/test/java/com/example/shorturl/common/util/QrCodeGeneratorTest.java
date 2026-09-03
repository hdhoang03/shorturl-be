package com.example.shorturl.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeGeneratorTest {

    @Test
    @DisplayName("should generate a valid base64 data URI for simple text")
    void generateSimpleQr() {
        Optional<String> result = QrCodeGenerator.generateQrCode("https://example.com", 250, 250);
        assertTrue(result.isPresent());
        assertTrue(result.get().startsWith("data:image/png;base64,"));
    }

    @Test
    @DisplayName("should generate QR for Unicode text")
    void generateUnicodeQr() {
        Optional<String> result = QrCodeGenerator.generateQrCode("Xin chào thế giới 🌍", 300, 300);
        assertTrue(result.isPresent());
        assertTrue(result.get().startsWith("data:image/png;base64,"));
    }

    @Test
    @DisplayName("should return empty for null text")
    void nullTextReturnsEmpty() {
        Optional<String> result = QrCodeGenerator.generateQrCode(null, 250, 250);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty for blank text")
    void blankTextReturnsEmpty() {
        Optional<String> result = QrCodeGenerator.generateQrCode("   ", 250, 250);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty for zero dimensions")
    void zeroDimensionsReturnsEmpty() {
        Optional<String> result = QrCodeGenerator.generateQrCode("test", 0, 250);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty for negative dimensions")
    void negativeDimensionsReturnsEmpty() {
        Optional<String> result = QrCodeGenerator.generateQrCode("test", -100, 250);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("backward-compatible method should return string or null")
    void backwardCompatibleMethod() {
        String result = QrCodeGenerator.generateQrCodeBase64("https://example.com", 250, 250);
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));

        // Null text should return null (backward compat)
        assertNull(QrCodeGenerator.generateQrCodeBase64(null, 250, 250));
    }

    @Test
    @DisplayName("different dimensions should produce different sized outputs")
    void differentDimensionsProduceDifferentOutput() {
        Optional<String> small = QrCodeGenerator.generateQrCode("test", 100, 100);
        Optional<String> large = QrCodeGenerator.generateQrCode("test", 500, 500);

        assertTrue(small.isPresent());
        assertTrue(large.isPresent());
        // Larger image should produce longer base64 string
        assertTrue(large.get().length() > small.get().length());
    }
}
