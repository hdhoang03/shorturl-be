package com.example.shorturl.service;

import com.example.shorturl.dto.request.ShortenUrlRequest;
import com.example.shorturl.dto.request.UpdateUrlRequest;
import com.example.shorturl.dto.response.ShortUrlResponse;

import java.util.List;

public interface UrlShortenerService {
    ShortUrlResponse createShortUrl(ShortenUrlRequest request);
    String getOriginalUrl(String shortCode);
    ShortUrlResponse getShortUrlDetails(String shortCode);
    List<ShortUrlResponse> getUrlsByCodes(List<String> shortCodes);
    ShortUrlResponse updateShortUrl(String shortCode, UpdateUrlRequest request);
    void deleteShortUrl(String shortCode);
}