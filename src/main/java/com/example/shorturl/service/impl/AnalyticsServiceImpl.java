package com.example.shorturl.service.impl;

import com.example.shorturl.common.exception.AppException;
import com.example.shorturl.common.exception.ErrorCode;
import com.example.shorturl.common.util.DeviceParser;
import com.example.shorturl.common.util.IpExtractor;
import com.example.shorturl.dto.response.ClickAnalyticsResponse;
import com.example.shorturl.entity.ClickLog;
import com.example.shorturl.entity.ShortUrl;
import com.example.shorturl.repository.ClickLogRepository;
import com.example.shorturl.repository.ShortUrlRepository;
import com.example.shorturl.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ClickLogRepository clickLogRepository;
    private final ShortUrlRepository shortUrlRepository;

    @Async("clickLoggingExecutor")
    @Override
    @Transactional
    public void logClickAsync(String shortCode, String ipAddress, String userAgent, String referer) {
        try {
            ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode).orElse(null);
            if (shortUrl == null) return;

            ClickLog clickLog = ClickLog.builder()
                    .shortUrlId(shortUrl.getId())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .deviceType(DeviceParser.parseDeviceType(userAgent))
                    .browser(DeviceParser.parseBrowser(userAgent))
                    .os(DeviceParser.parseOs(userAgent))
                    .referer(referer != null ? referer : "Direct")
                    .build();

            clickLogRepository.save(clickLog);
            shortUrlRepository.incrementClickCount(shortUrl.getId());
        } catch (Exception e) {
            log.error("Failed to log click for shortCode: {}", shortCode, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ClickAnalyticsResponse getAnalytics(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));

        Long shortUrlId = shortUrl.getId();
        long uniqueVisitors = clickLogRepository.countDistinctIpByShortUrlId(shortUrlId);

        return ClickAnalyticsResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .totalClicks(shortUrl.getClickCount())
                .uniqueVisitors(uniqueVisitors)
                .createdAt(shortUrl.getCreatedAt())
                .clicksByDevice(toMap(clickLogRepository.countClicksByDevice(shortUrlId)))
                .clicksByBrowser(toMap(clickLogRepository.countClicksByBrowser(shortUrlId)))
                .clicksByOs(toMap(clickLogRepository.countClicksByOs(shortUrlId)))
                .clicksByReferer(toMap(clickLogRepository.countClicksByReferer(shortUrlId)))
                .clicksByDate(toMap(clickLogRepository.countClicksByDate(shortUrlId)))
                .build();
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                map.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        return map;
    }
}