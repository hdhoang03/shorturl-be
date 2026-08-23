package com.example.shorturl.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlResponse {

    private Long id;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private String qrCodeBase64;

    /** Serialized as both clickCount (legacy) and totalClicks for frontend */
    private Long clickCount;

    public Long getTotalClicks() {
        return clickCount;
    }

    private LocalDateTime expiresAt;

    @JsonProperty("isActive")
    private Boolean isActive;

    /** Frontend uses 'active' field name */
    public Boolean getActive() {
        return isActive;
    }

    private LocalDateTime createdAt;
}