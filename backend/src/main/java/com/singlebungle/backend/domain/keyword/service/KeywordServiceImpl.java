package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

            if (isKeyword) {
                // Redis에서 현재 curCnt 값 가져오기
                String curCntStr = (String) keywordTemplate.opsForHash().get("keyword:" + name, "curCnt");
                // curCnt 값 증가 및 업데이트
                int curCnt = Integer.parseInt(curCntStr.toString()) + 1;
                keywordTemplate.opsForHash().put("keyword:" + name, "curCnt", String.valueOf(curCnt));

                continue;
            }

            // 키워드 저장
            Keyword kw = Keyword.convertToEntity(name);
            keywordRepository.save(kw);
            // Redis 데이터 초기 설정
            keywordTemplate.opsForHash().put("keyword:" + name, "prevCnt", "1");
            keywordTemplate.opsForHash().put("keyword:" + name, "curCnt", "1");

        }
    }
}
