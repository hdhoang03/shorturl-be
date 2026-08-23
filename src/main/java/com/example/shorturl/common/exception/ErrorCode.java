package com.example.shorturl.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    URL_NOT_FOUND(HttpStatus.NOT_FOUND, "Short URL does not exist or has been deleted"),
    URL_EXPIRED(HttpStatus.GONE, "This short link has expired"),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "Original URL format is invalid"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}