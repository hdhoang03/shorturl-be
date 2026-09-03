package com.example.shorturl.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceParserTest {

    // ── Real-world User-Agent strings ──

    private static final String CHROME_WINDOWS = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final String SAFARI_MAC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15";

    private static final String FIREFOX_LINUX = "Mozilla/5.0 (X11; Linux x86_64; rv:120.0) " +
            "Gecko/20100101 Firefox/120.0";

    private static final String EDGE_WINDOWS = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";

    private static final String CHROME_ANDROID = "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    private static final String SAFARI_IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

    private static final String SAFARI_IPAD = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

    private static final String OPERA_WINDOWS = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 OPR/106.0.0.0";

    @Nested
    @DisplayName("parseDeviceType()")
    class DeviceType {

        @Test
        void nullReturnsUnknown() {
            assertEquals("Unknown", DeviceParser.parseDeviceType(null));
        }

        @Test
        void blankReturnsUnknown() {
            assertEquals("Unknown", DeviceParser.parseDeviceType("   "));
        }

        @Test
        void desktopBrowsers() {
            assertEquals("Desktop", DeviceParser.parseDeviceType(CHROME_WINDOWS));
            assertEquals("Desktop", DeviceParser.parseDeviceType(SAFARI_MAC));
            assertEquals("Desktop", DeviceParser.parseDeviceType(FIREFOX_LINUX));
        }

        @Test
        void mobileBrowsers() {
            assertEquals("Mobile", DeviceParser.parseDeviceType(CHROME_ANDROID));
            assertEquals("Mobile", DeviceParser.parseDeviceType(SAFARI_IPHONE));
        }

        @Test
        void tabletBrowsers() {
            assertEquals("Tablet", DeviceParser.parseDeviceType(SAFARI_IPAD));
        }
    }

    @Nested
    @DisplayName("parseBrowser()")
    class Browser {

        @Test
        void nullReturnsUnknown() {
            assertEquals("Unknown", DeviceParser.parseBrowser(null));
        }

        @Test
        void detectsChrome() {
            assertEquals("Chrome", DeviceParser.parseBrowser(CHROME_WINDOWS));
        }

        @Test
        void detectsSafari() {
            assertEquals("Safari", DeviceParser.parseBrowser(SAFARI_MAC));
        }

        @Test
        void detectsFirefox() {
            assertEquals("Firefox", DeviceParser.parseBrowser(FIREFOX_LINUX));
        }

        @Test
        void detectsEdgeOverChrome() {
            // Edge UA contains "Chrome" but should be classified as Edge
            assertEquals("Edge", DeviceParser.parseBrowser(EDGE_WINDOWS));
        }

        @Test
        void detectsOperaOverChrome() {
            assertEquals("Opera", DeviceParser.parseBrowser(OPERA_WINDOWS));
        }
    }

    @Nested
    @DisplayName("parseOs()")
    class Os {

        @Test
        void nullReturnsUnknown() {
            assertEquals("Unknown", DeviceParser.parseOs(null));
        }

        @Test
        void detectsWindows() {
            assertEquals("Windows", DeviceParser.parseOs(CHROME_WINDOWS));
        }

        @Test
        void detectsMacOs() {
            assertEquals("macOS", DeviceParser.parseOs(SAFARI_MAC));
        }

        @Test
        void detectsLinux() {
            assertEquals("Linux", DeviceParser.parseOs(FIREFOX_LINUX));
        }

        @Test
        void detectsAndroid() {
            assertEquals("Android", DeviceParser.parseOs(CHROME_ANDROID));
        }

        @Test
        void detectsIos() {
            assertEquals("iOS", DeviceParser.parseOs(SAFARI_IPHONE));
        }
    }
}
