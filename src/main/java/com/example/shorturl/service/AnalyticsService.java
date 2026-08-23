package com.example.shorturl.service;

import com.example.shorturl.dto.response.ClickAnalyticsResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AnalyticsService {
//    void logClickAsync(String shortCode, HttpServletRequest request);
    ClickAnalyticsResponse getAnalytics(String shortCode);
    void logClickAsync(String shortCode, String ipAddress, String userAgent, String referer);
}