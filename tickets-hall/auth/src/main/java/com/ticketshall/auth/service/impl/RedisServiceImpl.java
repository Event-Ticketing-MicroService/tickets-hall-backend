package com.ticketshall.auth.service.impl;

import com.ticketshall.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final StringRedisTemplate stringRedisTemplate;

    public void setInRedis(String key, String value, long expirationInMinutes) {
        stringRedisTemplate.opsForValue().set(key, value, expirationInMinutes, TimeUnit.MINUTES);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
