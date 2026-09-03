package com.example.shorturl.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpExtractorTest {

    private HttpServletRequest mockRequest() {
        return mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("should extract IP from X-Forwarded-For header")
    void extractFromXForwardedFor() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50");

        assertEquals("203.0.113.50", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("should take first IP from comma-separated X-Forwarded-For")
    void extractFirstFromMultipleIps() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50, 70.41.3.18, 150.172.238.178");

        assertEquals("203.0.113.50", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("should skip 'unknown' header values")
    void skipUnknownHeaders() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");

        assertEquals("192.168.1.100", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("should fallback to remoteAddr when no headers present")
    void fallbackToRemoteAddr() {
        HttpServletRequest request = mockRequest();
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        assertEquals("10.0.0.1", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("should normalize IPv6 loopback to 127.0.0.1")
    void normalizeIpv6Loopback() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("0:0:0:0:0:0:0:1");

        assertEquals("127.0.0.1", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("should skip garbage header values")
    void skipGarbageHeaders() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("not-an-ip!");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        assertEquals("10.0.0.1", IpExtractor.getClientIp(request));
    }

    @Test
    @DisplayName("getClientIpSafe should return Optional.empty when no valid IP found")
    void safeMethodReturnsEmpty() {
        HttpServletRequest request = mockRequest();
        when(request.getRemoteAddr()).thenReturn(null);

        Optional<String> result = IpExtractor.getClientIpSafe(request);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getClientIpSafe should return Optional with valid IP")
    void safeMethodReturnsPresent() {
        HttpServletRequest request = mockRequest();
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.42");

        Optional<String> result = IpExtractor.getClientIpSafe(request);
        assertTrue(result.isPresent());
        assertEquals("198.51.100.42", result.get());
    }
}
