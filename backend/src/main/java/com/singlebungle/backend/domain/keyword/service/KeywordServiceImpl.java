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
import java.util.*;
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
//    @Cacheable(value = "keywordRankCache", key = "'topRanks'", unless = "#result == null || #result.isEmpty()")
    public List<KeywordRankResponseDTO> getKeywordRankList() {
        log.info(">>>>>>>>>> getKeywordRankList >>>>>>>>> 처리중 ...");

        // Redis에서 정확한 등락률 계산을 위해 상위 10위 랭킹 목록 가져오기
        Set<ZSetOperations.TypedTuple<Object>> currentRanks = keywordTemplate.opsForZSet()
                .reverseRangeWithScores("keyword-ranking", 0, 9); // 상위 10위 (0부터 9까지)

        log.info(">>> currentRanks : {}", Arrays.toString(currentRanks.toArray()));

        if (currentRanks == null || currentRanks.isEmpty()) {
            throw new EntityNotFoundException(">>> getKeywordRankList >>> 랭킹에 등록된 데이터가 없습니다.");
        }

        // Redis에서 이전 랭킹 데이터 가져오기 (상위 10위)
        Set<ZSetOperations.TypedTuple<Object>> previousRanks = keywordTemplate.opsForZSet()
                .reverseRangeWithScores("previous-ranking", 0, 9); // 이전 순위 데이터

        log.info(">>> previousRanks : {}", Arrays.toString(previousRanks.toArray()));

        // 이전 데이터 매핑 (키워드와 점수로 변환)
        Map<String, Double> previousRankMap = previousRanks != null
                ? previousRanks.stream()
                .collect(Collectors.toMap(
                        rank -> rank.getValue().toString(),
                        rank -> rank.getScore() != null ? rank.getScore() : 0.0
                ))
                : new HashMap<>();


        // 현재 랭킹과 이전 랭킹 비교
        List<KeywordRankResponseDTO> rankedKeywords = currentRanks.stream()
                .map(rank -> {
                    String keyword = rank.getValue().toString();
                    double currentScore = rank.getScore() != null ? rank.getScore() : 0.0;

                    // 이전 점수 가져오기 (없으면 기본값 0.0)
                    double previousScore = previousRankMap.getOrDefault(keyword, 0.0);

                    String isState;
                    if (currentScore > previousScore) {
                        isState = "up";
                    } else if (currentScore < previousScore) {
                        isState = "down";
                    } else {
                        isState = "same";
                    }

                    double gap = currentScore - previousScore;

                    log.info(">>> Keyword: {}, CurrentScore: {}, PreviousScore: {}, Gap: {}, State: {}",
                            keyword, currentScore, previousScore, gap, isState);

                    return new KeywordRankResponseDTO(keyword, isState, gap);
                })
                .sorted(Comparator.comparing(KeywordRankResponseDTO::getKeyword))
                .limit(5) // 상위 5개만 반환
                .collect(Collectors.toList());

        return rankedKeywords;
    }

}