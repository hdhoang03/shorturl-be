package com.example.shorturl.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlRequest {

    @NotBlank(message = "Original URL cannot be blank")
    @URL(message = "Invalid URL format. Must start with http:// or https://")
    private String originalUrl;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,10}$", message = "Custom alias must be 3-10 alphanumeric characters")
    private String customAlias;

    /** Number of days until the link expires. Null = no expiry. */
    @Min(value = 1, message = "Expiry must be at least 1 day")
    private Integer expiresInDays;
}