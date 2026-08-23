package com.example.shorturl.service.impl;

import com.example.shorturl.common.exception.AppException;
import com.example.shorturl.common.exception.ErrorCode;
import com.example.shorturl.common.util.Base62Encoder;
import com.example.shorturl.dto.request.ShortenUrlRequest;
import com.example.shorturl.dto.request.UpdateUrlRequest;
import com.example.shorturl.dto.response.ShortUrlResponse;
import com.example.shorturl.entity.ShortUrl;
import com.example.shorturl.mapper.UrlMapper;
import com.example.shorturl.repository.ShortUrlRepository;
import com.example.shorturl.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlMapper urlMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.base-url:http://localhost:8080/}")
    private String baseUrl;

    private static final String REDIS_URL_PREFIX = "url:";

    @Override
    @Transactional
    public ShortUrlResponse createShortUrl(ShortenUrlRequest request) {
        String customAlias = request.getCustomAlias();

        if (customAlias != null && !customAlias.isBlank()) {
            if (shortUrlRepository.existsByShortCode(customAlias)) {
                throw new AppException(ErrorCode.INVALID_URL_FORMAT, "Custom alias already exists");
            }
        }

        ShortUrl shortUrl = urlMapper.toEntity(request);

        // Convert expiresInDays → expiresAt
        if (request.getExpiresInDays() != null) {
            shortUrl.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
        }

        shortUrl = shortUrlRepository.save(shortUrl);

        String shortCode = (customAlias != null && !customAlias.isBlank())
                ? customAlias
                : Base62Encoder.encode(shortUrl.getId());

        shortUrl.setShortCode(shortCode);
        shortUrlRepository.save(shortUrl);

        cacheUrl(shortCode, shortUrl.getOriginalUrl(), shortUrl.getExpiresAt());

        return buildResponse(shortUrl);
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        String cacheKey = REDIS_URL_PREFIX + shortCode;
        String cachedOriginalUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedOriginalUrl != null) {
            return cachedOriginalUrl;
        }

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));

        if (!shortUrl.getIsActive()) {
            throw new AppException(ErrorCode.URL_NOT_FOUND, "This short link has been disabled");
        }

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.URL_EXPIRED);
        }

        cacheUrl(shortCode, shortUrl.getOriginalUrl(), shortUrl.getExpiresAt());
        return shortUrl.getOriginalUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public ShortUrlResponse getShortUrlDetails(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));
        return buildResponse(shortUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortUrlResponse> getUrlsByCodes(List<String> shortCodes) {
        return shortUrlRepository.findByShortCodeIn(shortCodes)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional
    public ShortUrlResponse updateShortUrl(String shortCode, UpdateUrlRequest request) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));

        // Only update fields that are actually provided
        if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            shortUrl.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getExpiresInDays() != null) {
            shortUrl.setExpiresAt(LocalDateTime.now().plusDays(request.getExpiresInDays()));
        }
        if (request.getActive() != null) {
            shortUrl.setIsActive(request.getActive());
        }

        shortUrlRepository.save(shortUrl);

        redisTemplate.delete(REDIS_URL_PREFIX + shortCode);
        if (shortUrl.getIsActive()) {
            cacheUrl(shortCode, shortUrl.getOriginalUrl(), shortUrl.getExpiresAt());
        }

        return buildResponse(shortUrl);
    }

    @Override
    @Transactional
    public void deleteShortUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new AppException(ErrorCode.URL_NOT_FOUND));

        shortUrlRepository.delete(shortUrl);
        redisTemplate.delete(REDIS_URL_PREFIX + shortCode);
    }

    private void cacheUrl(String shortCode, String originalUrl, LocalDateTime expiresAt) {
        String cacheKey = REDIS_URL_PREFIX + shortCode;
        if (expiresAt != null) {
            Duration duration = Duration.between(LocalDateTime.now(), expiresAt);
            if (!duration.isNegative()) {
                redisTemplate.opsForValue().set(cacheKey, originalUrl, duration);
                return;
            }
        }
        redisTemplate.opsForValue().set(cacheKey, originalUrl, Duration.ofDays(7));
    }

    private ShortUrlResponse buildResponse(ShortUrl entity) {
        ShortUrlResponse response = urlMapper.toResponse(entity);
        String fullShortUrl = baseUrl + entity.getShortCode();
        response.setShortUrl(fullShortUrl);

        // Sinh ảnh QR Code (250x250) dạng Base64
        response.setQrCodeBase64(com.example.shorturl.common.util.QrCodeGenerator.generateQrCodeBase64(fullShortUrl, 250, 250));

        return response;
    }
}