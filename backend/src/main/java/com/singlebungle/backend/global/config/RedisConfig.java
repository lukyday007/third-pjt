package com.singlebungle.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis-keyword.host}")
    private String redisKeywordHost;

    @Value("${spring.redis-keyword.port}")
    private int redisKeywordPort;

    @Value("${spring.redis-user.host}")
    private String redisUserHost;

    @Value("${spring.redis-user.port}")
    private int redisUserPort;

    // 기본 Redis 인스턴스 설정
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.setPassword(redisPassword);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory());

        return template;
    }


    // redis-keyword 인스턴스 설정
    @Bean
    public RedisConnectionFactory redisKeywordConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisKeywordHost, redisKeywordPort);
        factory.setPassword(redisPassword);
        return factory;
    }

    @Bean(name = "redisKeywordTemplate")
    public RedisTemplate<String, Object> redisKeywordTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisKeywordConnectionFactory());
        return template;
    }


    // redis-user 인스턴스 설정
    @Bean
    public RedisConnectionFactory redisUserConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisUserHost, redisUserPort);
        factory.setPassword(redisPassword);
        return factory;
    }

    @Bean(name = "redisUserTemplate")
    public RedisTemplate<String, String> redisUserTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisUserConnectionFactory());
        return template;
    }

}