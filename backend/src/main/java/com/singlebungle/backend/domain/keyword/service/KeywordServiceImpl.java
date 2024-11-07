package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final KeywordRepository keywordRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void saveKeyword(List<String> keywords) {

        for (String name : keywords) {
            boolean isKeyword = keywordRepository.existsByKeywordName(name);

            if (isKeyword) {
                Keyword tmp = keywordRepository.findByKeywordName(name);
                tmp.setUseCount(tmp.getUseCount() + 1);   // 키워드 사용 수 증가
                keywordRepository.save(tmp);
                continue;
            }

            // 키워드 저장
            Keyword kw = Keyword.convertToEntity(name);
            keywordRepository.save(kw);

        }
    }
}
