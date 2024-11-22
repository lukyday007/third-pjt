package com.singlebungle.backend.global.config;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
@EnableScheduling   // 스케줄링 기능 활성화 -> @Scheduled로 설정해서 주기적 실행
@RequiredArgsConstructor
@Slf4j
@EnableRetry        // 특정 작업 실패할 경우 재실행
public class SchedulerConfig implements SchedulingConfigurer {

    // SchedulingConfigurer : 스레드 풀 사용을 위한 스케줄링 설정
    // -> 병럴로 처리하기 때문에 요청이 폭증해도 작업 큐에서 순차적으로 처리, 성능 개선

    private final KeywordService keywordService;
    private final KeywordRepository keywordRepository;

    // @Qualifier로 특정 RedisTemplate을 주입받음
    @Qualifier("redisKeywordTemplate")
    private final RedisTemplate<String, Object> keywordTemplate;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // ThreadPoolTaskScheduler : 스레드 풀 구성, 병렬로 실행할 스케줄러 설정
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(10);    // 한 번에 10개의 스케줄링 작업 처리 (조정가능)
//        threadPoolTaskScheduler.setThreadNamePrefix();
        threadPoolTaskScheduler.initialize();
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }


    @Scheduled(fixedRate = 60 * 1000)    // 1분 마다 진행
    @CacheEvict(value = "keywordRankCache", key = "'ranking'")
    public void updateRanking() {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        log.info(">>>>>>>>>>>>>>>>>>   {}   updateKeywordRanking 시작... <<<<<<<<<<<<<<<<<<<<<", startTime);
        try {
            log.info(">>> 키워드 갱신");
            keywordService.updateKeywordRanking();

        } catch (RedisConnectionFailureException e) {
            log.error(">>> 키워드 랭킹 갱신 준 레디스 연결에 실패했습니다.", e);

        } catch (Exception e) {
            log.error(">>> 키워드 랭킹 갱신 중 예기치 못한 에러가 발생했습니다.", e);

        }

        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        log.info(">>> updateKeywordRanking 종료. 총 수행 시간: {}ms", (endTime - startTime));
        log.info(">>>>>>>>>>>>>>>>>>       updateKeywordRanking 끄읕      <<<<<<<<<<<<<<<<<<<<<");
    }


    // 12시간마다 SQL 데이터베이스와 동기화
    @Transactional
    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Seoul")
    public void updateSQL() {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        log.info(">>>>>>>>>>>>>>>>>>       updateSQL 시작      <<<<<<<<<<<<<<<<<<<<<");

        try {
            log.info(">>> keyword DB 갱신 중...");
            keywordService.updateSQLDB();

        } catch (RedisConnectionFailureException e) {
            log.error("SQL 업데이트 도중 Redis 연결이 끊어졌습니다.", e);
        } catch (Exception e) {
            log.error("SQL 업데이트 도중 예기지 못한 에러 발생.", e);
        }

        long endTime = System.currentTimeMillis(); // 종료 시간 기록
        log.info(">>> updateSQL 종료. 총 수행 시간: {}ms", (endTime - startTime));
        log.info(">>>>>>>>>>>>>>>>>>       updateSQL 끄읕      <<<<<<<<<<<<<<<<<<<<<");
    }
}
