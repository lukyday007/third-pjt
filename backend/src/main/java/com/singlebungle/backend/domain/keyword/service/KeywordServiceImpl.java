package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
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

    @Override
    @Transactional
    public void save(List<String> keywords) {

        for (String keyword : keywords) {
            boolean result = keywordRepository.existsByKeywordName(keyword);

            if (result) {
                continue;
            }

            // 키워드 저장
            Keyword kw = Keyword.convertToEntity(keyword);
            keywordRepository.save(kw);

        }
    }
}
