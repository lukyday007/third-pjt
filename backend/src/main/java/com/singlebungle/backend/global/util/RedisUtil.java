package com.singlebungle.backend.global.util;

import com.singlebungle.backend.global.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    public String getData(String key) {

        if (key == null) {
            throw new InvalidRequestException("RedisUtil : 키 값이 없습니다.");
        }
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();

        return valueOperations.get(key);
    }

    // 휴지통 bean 캐싱 만료 시 => 삭제
    public void setDataExpire(String key, String value) {

        Long term = 604800L;
        if (key == null) {
            throw new InvalidRequestException("RedisUtil : 키 값이 없습니다.");
        }

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expire = Duration.ofSeconds(term);
        valueOperations.set(key, value, expire);
    }

    public void deleteData(String key) {

        if(key == null) {
            throw new InvalidRequestException("RedisUtil : 키 값이 없습니다.");
        }

        redisTemplate.delete(key);
    }

}