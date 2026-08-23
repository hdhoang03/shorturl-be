package com.example.shorturl.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickAnalyticsResponse {

    private String shortCode;
    private String originalUrl;
    private Long totalClicks;
    private Long uniqueVisitors;
    private LocalDateTime createdAt;

    private Map<String, Long> clicksByDevice;
    private Map<String, Long> clicksByBrowser;
    private Map<String, Long> clicksByOs;
    private Map<String, Long> clicksByReferer;
    private Map<String, Long> clicksByDate;
}