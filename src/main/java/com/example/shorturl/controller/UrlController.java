package com.example.shorturl.controller;

import com.example.shorturl.common.response.ApiResponse;
import com.example.shorturl.dto.request.ShortenUrlRequest;
import com.example.shorturl.dto.request.UpdateUrlRequest;
import com.example.shorturl.dto.response.ClickAnalyticsResponse;
import com.example.shorturl.dto.response.ShortUrlResponse;
import com.example.shorturl.service.AnalyticsService;
import com.example.shorturl.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URL Management & Analytics", description = "APIs for shortening URLs, managing links, and viewing metrics")
public class UrlController {

    private final UrlShortenerService urlShortenerService;
    private final AnalyticsService analyticsService;

    @PostMapping("/shorten")
    @Operation(summary = "Shorten a long URL", description = "Encodes URL with Base62, generates short code and warms up Redis cache")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> shortenUrl(@Valid @RequestBody ShortenUrlRequest request) {
        ShortUrlResponse response = urlShortenerService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get short URL details", description = "Fetches metadata for a specific short link")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> getUrlDetails(@PathVariable String shortCode) {
        ShortUrlResponse response = urlShortenerService.getShortUrlDetails(shortCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/batch")
    @Operation(summary = "Get history by short codes", description = "Fetches link information for a list of short codes stored in client LocalStorage")
    public ResponseEntity<ApiResponse<List<ShortUrlResponse>>> getBatchUrls(@RequestBody List<String> shortCodes) {
        List<ShortUrlResponse> response = urlShortenerService.getUrlsByCodes(shortCodes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{shortCode}")
    @Operation(summary = "Update short URL", description = "Updates target URL, expiration time, or active status")
    public ResponseEntity<ApiResponse<ShortUrlResponse>> updateUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateUrlRequest request) {
        ShortUrlResponse response = urlShortenerService.updateShortUrl(shortCode, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete short URL", description = "Deletes link from database and removes from Redis cache")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable String shortCode) {
        urlShortenerService.deleteShortUrl(shortCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{shortCode}/analytics")
    @Operation(summary = "Get URL click analytics", description = "Aggregates total clicks, unique visitors, devices, OS, browsers, referrers, and timeline")
    public ResponseEntity<ApiResponse<ClickAnalyticsResponse>> getAnalytics(@PathVariable String shortCode) {
        ClickAnalyticsResponse response = analyticsService.getAnalytics(shortCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}