package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.repository.ImageDetailRepository;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.keyword.dto.KeywordRankResponseDTO;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
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
    private final DirectoryRepository directoryRepository;
    private final ImageManagementRepository imageManagementRepository;
    private final ImageDetailRepository imageDetailRepository;
    private final UserRepository userRepository;

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
//    @Cacheable(value = "keywordRankCache", key = "'ranking'", unless = "#result == null || #result.isEmpty()")
    public List<KeywordRankResponseDTO> getKeywordRankList() {

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

                    return new KeywordRankResponseDTO(keyword, isState, gap);
                })
                .sorted(Comparator.comparing(KeywordRankResponseDTO::getKeyword))
                .limit(5) // 상위 5개만 반환
                .collect(Collectors.toList());

        return rankedKeywords;
    }

    public List<String> getKeywords(Long userId, String keyword, Long directoryId, boolean bin) {

        // userId로 User 객체를 먼저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Directory> directories = new ArrayList<>();

        if (bin) {
            // bin이 true일 경우: 유저의 휴지통(status = 2) 디렉토리만 조회
            Directory directory = directoryRepository.findByUserAndStatus(user, 2)
                    .orElseThrow(() -> new EntityNotFoundException("No directories found in bin"));
            directories.add(directory);  // Optional에서 값을 가져와서 리스트에 추가
        } else {
            if (directoryId == 0) {
                // directoryId가 0일 경우: 유저의 기본 디렉토리(status = 0)만 조회
                Directory directory = directoryRepository.findByUserAndStatus(user, 0)
                        .orElseThrow(() -> new EntityNotFoundException("No default directory found"));
                directories.add(directory);
            } else if (directoryId == -1) {
                // directoryId가 -1일 경우: 유저의 모든 디렉토리에서 휴지통(status != 2) 제외 조회
                directories = directoryRepository.findByUserAndStatusNot(user, 2);
            } else {
                // 특정 directoryId로 디렉토리 조회
                Directory directory = directoryRepository.findByUserAndDirectoryId(user, directoryId)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Directory not found with the given ID"));
                directories.add(directory); // 찾은 디렉토리를 리스트에 추가
            }
        }

        // 디렉토리와 연결된 이미지 ID 추출
        List<Long> imageIds = imageManagementRepository.findImageIdsByDirectories(directories);

        // 이미지와 연결된 키워드 중 검색어(keyword)가 포함된 키워드 조회
        return imageDetailRepository.findKeywordsByImageIdsAndKeyword(imageIds, keyword);
    }

}