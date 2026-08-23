package com.example.shorturl.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUrlRequest {

    /** If provided, updates the redirect target. */
    @URL(message = "Invalid URL format")
    private String originalUrl;

    /** If provided, extends expiry to N days from now. */
    @Min(value = 1, message = "Expiry must be at least 1 day")
    private Integer expiresInDays;

    private Boolean active;
}