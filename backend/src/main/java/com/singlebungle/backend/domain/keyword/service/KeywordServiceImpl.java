package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.keyword.dto.KeywordRankResponseDTO;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final KeywordRepository keywordRepository;

    // @Qualifier로 특정 RedisTemplate을 주입받음
    @Qualifier("redisKeywordTemplate")
    private final RedisTemplate<String, Object> keywordTemplate;

    @Override
    @Transactional
    public void saveKeyword(List<String> keywords) {
        /*
            키워드가 새로 저장되면 일단 redisKeyword에 저장
            중복된 키워드가 있으면 redisKeyword의 value + 1
            레디스에 다시 저장
        */
        for (String name : keywords) {
            boolean isKeyword = keywordRepository.existsByKeywordName(name);

            // 현재 시간 가져오기
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));

            if (isKeyword) {
                // Redis에서 현재 curCnt 값 가져오기
                String curCntStr = (String) keywordTemplate.opsForHash().get("keyword", name + ":curCnt");
                if (curCntStr == null) {
                    // curCnt가 없는 경우 초기값 설정
                    log.warn(">>> redis >>> curCntStr 이 null 일 때 curCnt, prevCnt 값 셋팅");
                    keywordTemplate.opsForHash().put("keyword", name + ":curCnt", "1");
                    keywordTemplate.opsForHash().put("keyword", name + ":prevCnt", "1");
                    keywordTemplate.opsForHash().put("keyword", name + ":updated", currentTime);

                } else {
                    int curCnt = Integer.parseInt(curCntStr) + 1;
                    keywordTemplate.opsForHash().put("keyword", name + ":curCnt", String.valueOf(curCnt));
                    keywordTemplate.opsForHash().put("keyword", name + ":updated", currentTime); // 업데이트 시간 저장
                }
                continue;
            }

            // 키워드 새로 저장
            Keyword kw = Keyword.convertToEntity(name);
            keywordRepository.save(kw);

            // Redis 데이터 초기 설정 (하나의 해시 키 "keyword"에 저장)
            keywordTemplate.opsForHash().put("keyword", name + ":prevCnt", "1");
            keywordTemplate.opsForHash().put("keyword", name + ":curCnt", "1");
            keywordTemplate.opsForHash().put("keyword", name + ":updated", currentTime);

        }
    }

    @Override
    public void increaseCurCnt(String keyword) {
        // 현재 시간 가져오기
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));

        if (keywordRepository.existsByKeywordName(keyword)) {
            String curCntStr = (String) keywordTemplate.opsForHash().get("keyword", keyword + ":curCnt");

            if (curCntStr == null)
                throw new EntityNotFoundException(">>> 레디스에 해당 키워드의 curCnt 데이터가 없습니다.");

            int curCnt = Integer.parseInt(curCntStr);
            ++ curCnt;

            keywordTemplate.opsForHash().put("keyword", keyword + ":curCnt", String.valueOf(curCnt));
            keywordTemplate.opsForHash().put("keyword", keyword + ":updated", currentTime);
        }
    }


    @Override
    @Cacheable(value = "keywordRankCache", key = "'topRanks'", unless = "#result == null || #result.isEmpty()")
    public List<KeywordRankResponseDTO> getKeywordRankList() {
        // Redis에서 상위 5위 데이터를 내림차순으로 조회
        Set<ZSetOperations.TypedTuple<Object>> ranks = keywordTemplate.opsForZSet()
                .reverseRangeWithScores("keyword-ranking", 0, 4); // 상위 5위 (0부터 4까지)

        if (ranks == null || ranks.isEmpty()) {
            throw new EntityNotFoundException(">>> getKeywordRankList >>> 랭킹에 등록된 데이터가 없습니다.");
        }

        return ranks.stream()
                .map(rank -> new KeywordRankResponseDTO(
                        rank.getValue().toString(), // Object를 String으로 변환
                        rank.getScore() != null ? rank.getScore().toString() : "0" // 점수 값 (null 처리)
                ))
                .collect(Collectors.toList());
    }

}