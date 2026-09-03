package com.example.shorturl.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Generates QR code images as Base64-encoded data URIs.
 * <p>
 * Uses ZXing with configurable encoding hints (UTF-8 charset, error correction level M,
 * and clean margins). Results are returned as {@link Optional} to avoid null propagation.
 */
public final class QrCodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(QrCodeGenerator.class);

    /** Reusable writer — QRCodeWriter is stateless and thread-safe. */
    private static final QRCodeWriter QR_WRITER = new QRCodeWriter();

    /** Default encoding hints for high-quality QR output. */
    private static final Map<EncodeHintType, Object> DEFAULT_HINTS = Map.of(
            EncodeHintType.CHARACTER_SET, "UTF-8",
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 1
    );

    private QrCodeGenerator() {}

    /**
     * Generates a QR code as a Base64 data URI string.
     *
     * @param text   the content to encode (must not be null or blank)
     * @param width  image width in pixels (must be > 0)
     * @param height image height in pixels (must be > 0)
     * @return an {@link Optional} containing the data URI, or empty on failure
     */
    public static Optional<String> generateQrCode(String text, int width, int height) {
        return generateQrCode(text, width, height, DEFAULT_HINTS);
    }

    /**
     * Generates a QR code with custom encoding hints.
     *
     * @param text   the content to encode
     * @param width  image width in pixels
     * @param height image height in pixels
     * @param hints  ZXing encode hints (charset, error correction, margin, etc.)
     * @return an {@link Optional} containing the data URI, or empty on failure
     */
    public static Optional<String> generateQrCode(String text, int width, int height,
                                                   Map<EncodeHintType, Object> hints) {
        if (text == null || text.isBlank()) {
            log.warn("Cannot generate QR code for null or blank text");
            return Optional.empty();
        }
        if (width <= 0 || height <= 0) {
            log.warn("Invalid QR code dimensions: {}x{}", width, height);
            return Optional.empty();
        }

        try {
            BitMatrix bitMatrix = QR_WRITER.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
                byte[] qrBytes = outputStream.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(qrBytes);
                return Optional.of("data:image/png;base64," + base64);
            }
        } catch (WriterException e) {
            log.error("Failed to encode QR code for text [{}]: {}", text, e.getMessage(), e);
            return Optional.empty();
        } catch (IOException e) {
            log.error("Failed to write QR code image: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Backward-compatible method that returns null on failure.
     * Prefer {@link #generateQrCode(String, int, int)} for new code.
     *
     * @param text   the content to encode
     * @param width  image width in pixels
     * @param height image height in pixels
     * @return the Base64 data URI or null on failure
     */
    public static String generateQrCodeBase64(String text, int width, int height) {
        return generateQrCode(text, width, height).orElse(null);
    }
}