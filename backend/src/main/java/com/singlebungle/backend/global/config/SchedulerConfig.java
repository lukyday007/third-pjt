package com.singlebungle.backend.global.config;

import ch.qos.logback.core.net.ObjectWriter;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.Transactional;

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

    // todo 테스트 후 2시간 간격으로 수정
    @Scheduled(fixedRate = 60 * 1000)  // 2 *60*60*1000 : 2시간마다 실행
    public void updateKeywordRanking() {

        // 빠른 검색 속도: Set은 중복을 허용하지 않고, contains와 같은 검색 연산이 더 효율적입니다.
        // 명시적으로 키가 중복되지 않음을 나타냄
        Set<String> keywords = keywordTemplate.keys("keyword:*");

        if (keywords != null) {
            for (Object  keywordObj  : keywords) {
                String keyword = (String) keywordObj;
                String prevCntStr = (String) keywordTemplate.opsForHash().get(keyword, "prevCnt");
                String curCntStr = (String) keywordTemplate.opsForHash().get(keyword, "curCnt");

                if (prevCntStr != null && curCntStr != null) {
                    int prevCnt = Integer.parseInt(prevCntStr);
                    int curCnt = Integer.parseInt(curCntStr);
                    int gap = curCnt - prevCnt;

                    // incrementScore 는 key가 이미 있을 경우 갱신, 없을 경우 add
                    keywordTemplate.opsForZSet().incrementScore("keyword-ranking:" + keyword, keyword, gap);
                    keywordTemplate.opsForHash().put(keyword, "prevCnt", String.valueOf(curCnt));
                }
            }
        }
    }

    // 12시간 간격으로 sql 갱신
    @Transactional
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000)  // 12시간마다 실행
    public void updateSQL() {
        Set<String> keywords = keywordTemplate.keys("keyword:*");

        if (keywords != null) {
            for (Object  keywordObj  : keywords) {
                String keyword = (String) keywordObj;
                String[] parts = keyword.split(":");
                String keywordParsed = parts[1];

                String curCntStr = (String) keywordTemplate.opsForHash().get(keyword, "curCnt");

                if (curCntStr != null) {
                    int curCnt = Integer.parseInt(curCntStr);
                    Keyword kw = keywordRepository.findByKeywordName(keywordParsed);
                    if (kw != null) {
                        kw.setUseCount(curCnt);
                        keywordRepository.save(kw);
                    }

                }
            }
        }
    }
}
