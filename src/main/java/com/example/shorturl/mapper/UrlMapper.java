package com.example.shorturl.mapper;

import com.example.shorturl.dto.request.ShortenUrlRequest;
import com.example.shorturl.dto.response.ShortUrlResponse;
import com.example.shorturl.entity.ShortUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UrlMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shortCode", ignore = true)
    @Mapping(target = "clickCount", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShortUrl toEntity(ShortenUrlRequest request);

    @Mapping(target = "shortUrl", ignore = true)
    @Mapping(target = "qrCodeBase64", ignore = true)
    ShortUrlResponse toResponse(ShortUrl entity);
}