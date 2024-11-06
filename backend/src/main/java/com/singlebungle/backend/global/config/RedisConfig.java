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
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
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
    public RedisTemplate<String, Object> redisUserTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisUserConnectionFactory());
        return template;
    }



//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
//        redisConfig.setHostName(host);
//        redisConfig.setPort(port);
//
////        redisConfig.setDatabase(databaseIndex); // 데이터베이스 인덱스 설정
////        redisConfig.setPassword(RedisPassword.of(password)); // 인증 설정
//
//        return new LettuceConnectionFactory(redisConfig);
//    }

}