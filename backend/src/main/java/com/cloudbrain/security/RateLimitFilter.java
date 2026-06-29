package com.cloudbrain.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * K8: 通用 API 限流过滤器
 * 基于 Redis 滑动窗口，按用户维度限制每分钟请求次数
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.rate-limit.max-per-minute:60}")
    private int maxPerMinute;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求属性获取当前用户 ID（由 JwtAuthFilter 设置）
        String userId = (String) request.getAttribute("currentUserId");
        if (userId == null) {
            // 未认证用户不限流
            filterChain.doFilter(request, response);
            return;
        }

        String minuteKey = "rate:limit:" + userId + ":"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        Long count = redisTemplate.opsForValue().increment(minuteKey);
        if (count != null && count == 1) {
            redisTemplate.expire(minuteKey, Duration.ofSeconds(60));
        }

        if (count != null && count > maxPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", "60");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
