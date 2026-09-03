package com.example.shorturl.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Extracts the real client IP address from an {@link HttpServletRequest},
 * respecting common reverse-proxy headers.
 * <p>
 * Validates extracted IPs using {@link InetAddress} to reject garbage values.
 * When running behind a load balancer, only the first IP in X-Forwarded-For
 * (the original client) is returned; subsequent proxy IPs are ignored.
 */
public final class IpExtractor {

    private static final Logger log = LoggerFactory.getLogger(IpExtractor.class);

    /** Headers checked in priority order (most commonly set by proxies first). */
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    /**
     * Regex to validate IPv4 and IPv6 addresses at a basic level.
     * More thorough validation is done via InetAddress.getByName().
     */
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})" +  // IPv4
            "|([0-9a-fA-F:]+)$"                                         // IPv6
    );

    private IpExtractor() {}

    /**
     * Extracts and validates the client IP address from the request.
     *
     * @param request the HTTP servlet request
     * @return an {@link Optional} containing the validated IP, or empty if
     *         no valid IP could be determined
     */
    public static Optional<String> getClientIpSafe(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For may contain multiple IPs: client, proxy1, proxy2
                String candidate = value.contains(",")
                        ? value.split(",")[0].trim()
                        : value.trim();

                if (isValidIp(candidate)) {
                    return Optional.of(normalizeIp(candidate));
                } else {
                    log.warn("Suspicious IP header [{}]: '{}' — skipping", header, candidate);
                }
            }
        }

        // Fallback to servlet remote address
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && isValidIp(remoteAddr)) {
            return Optional.of(normalizeIp(remoteAddr));
        }

        log.warn("Could not determine a valid client IP from request");
        return Optional.empty();
    }

    /**
     * Backward-compatible method that returns the client IP as a plain String.
     * Falls back to {@code request.getRemoteAddr()} if no valid IP is found.
     *
     * @param request the HTTP servlet request
     * @return the client IP address string
     */
    public static String getClientIp(HttpServletRequest request) {
        return getClientIpSafe(request)
                .orElseGet(request::getRemoteAddr);
    }

    /**
     * Validates an IP string using pattern matching and {@link InetAddress}.
     */
    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank()) return false;
        if (!IP_PATTERN.matcher(ip).matches()) return false;

        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * Normalizes IPv6 loopback (0:0:0:0:0:0:0:1) to 127.0.0.1 for consistency.
     */
    private static String normalizeIp(String ip) {
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }
}