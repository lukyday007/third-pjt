package com.singlebungle.backend.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
public class RedisCacheConfig {

    private final RedisTemplate<String, Object> redisKeywordTemplate;

    /*
     * RedisConfig에서 redisKeywordTemplate을 주입받아 사용
     */
    public RedisCacheConfig(@Qualifier("redisKeywordTemplate") RedisTemplate<String, Object> redisKeywordTemplate) {
        this.redisKeywordTemplate = redisKeywordTemplate; // redisKeywordTemplate을 중심으로 적용
    }

    /*
     * RedisCacheManager 설정 (redisKeywordTemplate 기반)
     */
    @Bean
    public CacheManager keywordCacheManager() {
        RedisConnectionFactory connectionFactory = redisKeywordTemplate.getConnectionFactory();

        // RedisCacheConfiguration 기본 설정
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // Redis Key 직렬화 방식
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // RedisConfig와 동일한 Key Serializer
                // Redis Value 직렬화 방식
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())) // RedisConfig와 동일한 Value Serializer

                /*
                     // 데이터의 만료기간(TTL) 설정
                    .entryTtl(Duration.ofSeconds(5)); 5s
                    .entryTtl(Duration.ofDays(1)); 1d
                    .entryTtl(Duration.ofMinutes(30)); 30m
                    .entryTtl(Duration.ofHours(1L));
                */

                .entryTtl(Duration.ofMinutes(2)); // 캐시 데이터의 TTL(1시간) 설정

        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory) // redisKeywordTemplate에서 가져온 connectionFactory 사용
                .cacheDefaults(redisCacheConfiguration)
                .build();

    }
}
