package com.example.shorturl.common.util;

import java.util.List;

/**
 * Parses User-Agent strings to extract device type, browser, and OS information.
 * <p>
 * Uses ordered rule-based matching for accuracy — rules are evaluated top-to-bottom,
 * so more specific patterns (e.g., "Edg" before "Chrome") take precedence.
 * For production-grade accuracy, consider migrating to ua-parser (uap-java).
 */
public final class DeviceParser {

    private DeviceParser() {}

    // -- Device Type Rules (order matters: tablet before mobile to avoid "iPad" matching "mobile") --

    private record DeviceRule(String keyword, String deviceType) {}

    private static final List<DeviceRule> DEVICE_RULES = List.of(
            new DeviceRule("tablet", "Tablet"),
            new DeviceRule("ipad", "Tablet"),
            new DeviceRule("kindle", "Tablet"),
            new DeviceRule("silk", "Tablet"),
            new DeviceRule("mobile", "Mobile"),
            new DeviceRule("android", "Mobile"),
            new DeviceRule("iphone", "Mobile"),
            new DeviceRule("ipod", "Mobile"),
            new DeviceRule("windows phone", "Mobile"),
            new DeviceRule("blackberry", "Mobile")
    );

    // -- Browser Rules (order matters: Edge/Opera before Chrome/Safari) --

    private record BrowserRule(String keyword, String browserName) {}

    private static final List<BrowserRule> BROWSER_RULES = List.of(
            new BrowserRule("edg", "Edge"),
            new BrowserRule("opr", "Opera"),
            new BrowserRule("opera", "Opera"),
            new BrowserRule("vivaldi", "Vivaldi"),
            new BrowserRule("brave", "Brave"),
            new BrowserRule("samsungbrowser", "Samsung Internet"),
            new BrowserRule("ucbrowser", "UC Browser"),
            new BrowserRule("firefox", "Firefox"),
            new BrowserRule("chrome", "Chrome"),    // must be after Edge/Opera/Brave/Vivaldi
            new BrowserRule("safari", "Safari"),    // must be after Chrome
            new BrowserRule("msie", "Internet Explorer"),
            new BrowserRule("trident", "Internet Explorer")
    );

    // -- OS Rules --

    private record OsRule(String keyword, String osName) {}

    private static final List<OsRule> OS_RULES = List.of(
            new OsRule("windows phone", "Windows Phone"),
            new OsRule("iphone", "iOS"),
            new OsRule("ipad", "iOS"),
            new OsRule("ipod", "iOS"),
            new OsRule("windows", "Windows"),
            new OsRule("mac os", "macOS"),
            new OsRule("macintosh", "macOS"),
            new OsRule("cros", "Chrome OS"),
            new OsRule("android", "Android"),
            new OsRule("linux", "Linux"),
            new OsRule("ubuntu", "Linux"),
            new OsRule("freebsd", "FreeBSD")
    );

    /**
     * Parses the device type from a User-Agent string.
     *
     * @param userAgent raw User-Agent header value
     * @return "Mobile", "Tablet", or "Desktop"
     */
    public static String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        String ua = userAgent.toLowerCase();
        for (DeviceRule rule : DEVICE_RULES) {
            if (ua.contains(rule.keyword())) {
                return rule.deviceType();
            }
        }
        return "Desktop";
    }

    /**
     * Parses the browser name from a User-Agent string.
     *
     * @param userAgent raw User-Agent header value
     * @return browser name or "Other"
     */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        String ua = userAgent.toLowerCase();
        for (BrowserRule rule : BROWSER_RULES) {
            if (ua.contains(rule.keyword())) {
                return rule.browserName();
            }
        }
        return "Other";
    }

    /**
     * Parses the operating system from a User-Agent string.
     *
     * @param userAgent raw User-Agent header value
     * @return OS name or "Other"
     */
    public static String parseOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        String ua = userAgent.toLowerCase();
        for (OsRule rule : OS_RULES) {
            if (ua.contains(rule.keyword())) {
                return rule.osName();
            }
        }
        return "Other";
    }
}