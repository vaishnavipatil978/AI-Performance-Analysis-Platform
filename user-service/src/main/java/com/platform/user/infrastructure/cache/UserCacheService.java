package com.platform.user.infrastructure.cache;

import com.platform.user.application.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCacheService {

    private static final String KEY_PREFIX = "user:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<UserResponse> get(UUID userId) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (value instanceof UserResponse response) {
            return Optional.of(response);
        }
        return Optional.empty();
    }

    public void cache(UUID userId, UserResponse response) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, response, TTL);
    }

    public void evict(UUID userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
