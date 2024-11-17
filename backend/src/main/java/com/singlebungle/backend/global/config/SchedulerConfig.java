package com.singlebungle.backend.global.config;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
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
import java.util.Map;
import java.util.Set;


@Configuration
@EnableScheduling   // 스케줄링 기능 활성화 -> @Scheduled로 설정해서 주기적 실행
@RequiredArgsConstructor
@Slf4j
@EnableRetry        // 특정 작업 실패할 경우 재실행
public class SchedulerConfig implements SchedulingConfigurer {

    // SchedulingConfigurer : 스레드 풀 사용을 위한 스케줄링 설정
    // -> 병럴로 처리하기 때문에 요청이 폭증해도 작업 큐에서 순차적으로 처리, 성능 개선

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


    // todo 테스트 후 2시간 간격으로 수정 2 * 60 * 60 * 1000 : 2시간마다 실행
//    @Scheduled(fixedRate = 60 * 60 * 1000)    // 1시간마다 실행
    @Scheduled(fixedRate = 30 * 60 * 1000)    // *** test 용 10분마다 실행 ***
    @CacheEvict(value = "keywordRankCache", key = "'ranking'")
    public void updateKeywordRanking() {
        try {
            Map<Object, Object> keywordMap = keywordTemplate.opsForHash().entries("keyword");

            if (keywordMap != null) {
                for (Map.Entry<Object, Object> temp : keywordMap.entrySet()) {
                    String fullField = temp.getKey().toString();
                    String[] parts = fullField.split(":");
                    if (parts.length != 2) {
                        log.warn("부적절한 데이터: {}", fullField);
                        continue;
                    }

                    String keyword = parts[0];      // {keyword}
                    String fieldType = parts[1];    // curCnt, prevCnt

                    // 현재 값 curCnt를 기준으로 {keyword} 조회
                    if ("curCnt".equals(fieldType)) {
                        String curCntStr = temp.getValue() != null ? temp.getValue().toString() : null;
                        String prevCntStr = keywordMap.get(keyword + ":prevCnt") != null ? keywordMap.get(keyword + ":prevCnt").toString() : null;

                        if (prevCntStr != null && curCntStr != null) {
                            try {
                                int prevCnt = Integer.parseInt(prevCntStr);
                                int curCnt = Integer.parseInt(curCntStr);
                                int gap = curCnt - prevCnt;

                                if (gap > 0) {
                                    // 이전 점수 가져오기, 없으면 0으로 초기화
                                    Double currentScore = keywordTemplate.opsForZSet().score("keyword-ranking", keyword);
                                    if (currentScore == null) {
                                        // keyword-ranking에 키워드가 없을 경우 초기화
                                        keywordTemplate.opsForZSet().add("keyword-ranking", keyword, 0.0);
                                        currentScore = 0.0;
                                    }

                                    Double previousScore = keywordTemplate.opsForZSet().score("previous-ranking", keyword);
                                    if (previousScore == null) {
                                        // previous-ranking에 키워드가 없을 경우 초기화
                                        keywordTemplate.opsForZSet().add("previous-ranking", keyword, 0.0);
                                        previousScore = 0.0;
                                    }

                                    // keyword-ranking 갱신
                                    keywordTemplate.opsForZSet().incrementScore("keyword-ranking", keyword, gap);

                                    // previous-ranking 갱신
                                    keywordTemplate.opsForZSet().incrementScore("previous-ranking", keyword, currentScore);
                                }

                                // 해쉬 객제의 이전 값을 현재 값으로 갱신
                                keywordTemplate.opsForHash().put("keyword", keyword + ":prevCnt", String.valueOf(curCnt));

                            } catch (NumberFormatException e) {
                                log.error("Number format exception for keyword: {}, prevCnt: {}, curCnt: {}", keyword, prevCntStr, curCntStr, e);
                            }
                        } else {
                            log.warn("현재 값 혹은 이전 값이 없는 키워드: {}", keyword);
                        }
                    }
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.error(">>> 키워드 랭킹 갱신 준 레디스 연결에 실패했습니다.", e);
        } catch (Exception e) {
            log.error(">>> 키워드 랭킹 갱신 중 예기치 못한 에러가 발생했습니다.", e);
        }
    }


    // 12시간마다 SQL 데이터베이스와 동기화
    @Transactional
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)  // text 용 2 시간 마다
    public void updateSQL() {
        try {
            Map<Object, Object> keywordMap = keywordTemplate.opsForHash().entries("keyword");

            if (keywordMap != null) {
                for (Map.Entry<Object, Object> temp : keywordMap.entrySet()) {
                    String fullField = temp.getKey().toString();
                    String[] parts = fullField.split(":");
                    if (parts.length != 2) {
                        log.warn("부적절한 데이터: {}", fullField);
                        continue;
                    }

                    String keyword = parts[0];
                    String fieldType = parts[1];

                    if (fieldType.equals("curCnt")) {
                        // curCnt 값을 가져와서 SQL 데이터베이스에 저장
                        String curCntStr = temp.getValue() != null ? temp.getValue().toString() : null;

                        if (curCntStr != null) {
                            try {
                                int curCnt = Integer.parseInt(curCntStr);
                                Keyword kw = keywordRepository.findByKeywordName(keyword);
                                if (kw != null) {
                                    kw.setUseCount(curCnt);
                                    keywordRepository.save(kw);
                                }
                            } catch (NumberFormatException e) {
                                log.error(">>> Number format exception for keyword: {}, curCnt: {}", keyword, curCntStr, e);
                            } catch (DataAccessException e) {
                                log.error(">>> Database access error while saving keyword: {}", keyword, e);
                            }
                        }
                    }
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure while updating SQL.", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred during update SQL.", e);
        }
    }
}
