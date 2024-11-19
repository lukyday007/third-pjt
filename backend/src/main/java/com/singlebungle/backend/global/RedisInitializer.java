package com.singlebungle.backend.global;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j

public class RedisInitializer {

    @Qualifier("redisKeywordTemplate")
    private final RedisTemplate<String, Object> keywordTemplate;

    public RedisInitializer(RedisTemplate<String, Object> keywordTemplate) {
        this.keywordTemplate = keywordTemplate;
    }


    @EventListener(ApplicationReadyEvent.class) // 서버 부팅 시 실행
    public void initializeRedis() {
        log.info(">>> Redis 초기화 시작 <<<");

        // 여러 키워드 초기화
        Set<Object> keywordKeys = keywordTemplate.opsForHash().keys("keyword");

        // 키 파싱: {keyword}:curCnt -> {keyword}
        Set<String> parsedKeywords = keywordKeys.stream()
                .map(key -> key.toString().split(":")[0]) // {keyword}:curCnt -> {keyword}
                .collect(Collectors.toSet());

        // Redis 데이터가 없는 경우 처리
        if (parsedKeywords.isEmpty()) {
            log.warn(">>> Redis 해시 'keyword'에 유효한 키가 존재하지 않습니다. 초기화 스킵 <<<");
            return;
        }

        // keyword-ranking 초기화
        if (!keywordTemplate.hasKey("keyword-ranking")) {
            for (String keyword : parsedKeywords) {
                keywordTemplate.opsForZSet().add("keyword-ranking", keyword, 0.0);
            }
            log.info(">>> keyword-ranking 초기화 완료 <<<");
        }

        // previous-ranking 초기화
        if (!keywordTemplate.hasKey("previous-ranking")) {
            for (String keyword : parsedKeywords) {
                keywordTemplate.opsForZSet().add("previous-ranking", keyword, 0.0);
            }
            log.info(">>> previous-ranking 초기화 완료 <<<");
        }

        log.info(">>> Redis 초기화 완료 <<<");
    }
}
