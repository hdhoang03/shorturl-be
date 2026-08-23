package com.example.shorturl.config;

import com.example.shorturl.common.exception.AppException;
import com.example.shorturl.common.exception.ErrorCode;
import com.example.shorturl.common.util.IpExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Chỉ giới hạn POST request (Tạo link mới)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = IpExtractor.getClientIp(request);
        String key = RATE_LIMIT_PREFIX + clientIp;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        // Thiết lập TTL 60s cho lần request đầu tiên
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        if (currentCount != null && currentCount > MAX_REQUESTS_PER_MINUTE) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED, "Tạo link quá nhanh. Tối đa 10 link / phút");
        }

        return true;
    }
}