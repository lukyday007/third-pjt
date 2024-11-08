package com.singlebungle.backend.global.config;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // todo 테스트 후 2시간 간격으로 수정 2 *60*60*1000 : 2시간마다 실행
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void updateKeywordRanking() {
        try {
            Set<Object> keywordObjs = keywordTemplate.opsForHash().keys("keyword");

            if (keywordObjs != null) {
                for (Object keywordObj : keywordObjs) {
                    String fullField = keywordObj.toString();  // 예: "apple:curCnt"
                    String[] parts = fullField.split(":");
                    if (parts.length < 2) continue;

                    String keyword = parts[0];
                    String fieldType = parts[1];

                    if (fieldType.equals("curCnt")) {
                        // prevCnt와 curCnt 값 가져오기
                        String prevCntStr = (String) keywordTemplate.opsForHash().get("keyword", keyword + ":prevCnt");
                        String curCntStr = (String) keywordTemplate.opsForHash().get("keyword", keyword + ":curCnt");

                        if (prevCntStr != null && curCntStr != null) {
                            try {
                                int prevCnt = Integer.parseInt(prevCntStr);
                                int curCnt = Integer.parseInt(curCntStr);
                                int gap = curCnt - prevCnt;

                                // ZSet에서 keyword-ranking 업데이트
                                keywordTemplate.opsForZSet().incrementScore("keyword-ranking", keyword, gap);

                                // prevCnt 값을 curCnt로 갱신
                                keywordTemplate.opsForHash().put("keyword", keyword + ":prevCnt", String.valueOf(curCnt));
                            } catch (NumberFormatException e) {
                                log.error("Number format exception for keyword: {}, prevCnt: {}, curCnt: {}", keyword, prevCntStr, curCntStr, e);
                            }
                        }
                    }
                }
            }
        } catch (RedisConnectionFailureException e) {
            log.error(">>> Redis connection failure while updating keyword ranking.", e);
        } catch (Exception e) {
            log.error(">>> Unexpected error occurred during update Keyword Ranking.", e);
        }
    }

    // 12시간마다 SQL 데이터베이스와 동기화
    @Transactional
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)
    public void updateSQL() {
        try {
            Set<Object> keywordObjs = keywordTemplate.opsForHash().keys("keyword");

            if (keywordObjs != null) {
                for (Object keywordObj : keywordObjs) {
                    String fullField = keywordObj.toString();
                    String[] parts = fullField.split(":");
                    if (parts.length < 2) continue;

                    String keyword = parts[0];
                    String fieldType = parts[1];

                    if (fieldType.equals("curCnt")) {
                        // curCnt 값을 가져와서 SQL 데이터베이스에 저장
                        String curCntStr = (String) keywordTemplate.opsForHash().get("keyword", keyword + ":curCnt");

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
