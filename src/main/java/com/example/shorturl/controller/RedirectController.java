package com.example.shorturl.controller;

import com.example.shorturl.common.util.IpExtractor;
import com.example.shorturl.service.AnalyticsService;
import com.example.shorturl.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect Endpoint", description = "High-performance URL Redirection")
public class RedirectController {

    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    @Operation(summary = "Redirect to original URL", description = "Redirects with HTTP 302 Found and logs analytics asynchronously")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String originalUrl = urlShortenerService.getOriginalUrl(shortCode);

        // Bóc tách dữ liệu chuỗi tại luồng chính trước khi request bị Tomcat recycle
        String ipAddress = IpExtractor.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        // Đẩy dữ liệu thô sang ThreadPool chạy ngầm an toàn
        analyticsService.logClickAsync(shortCode, ipAddress, userAgent, referer);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }
}