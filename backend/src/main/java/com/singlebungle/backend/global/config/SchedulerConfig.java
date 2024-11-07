package com.singlebungle.backend.global.config;

import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@EnableRetry
public class SchedulerConfig {

    private final KeywordRepository keywordRepository;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedRate = )


}
