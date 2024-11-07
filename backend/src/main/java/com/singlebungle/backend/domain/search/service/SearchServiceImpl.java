package com.singlebungle.backend.domain.search.service;

import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.search.entity.SearchDocument;
import com.singlebungle.backend.domain.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;
    private final KeywordRepository keywordRepository;

    @Override
    public void saveTags(List<String> tags, String imageUrl) {

        // imageUrl 기준으로 중복 여부 체크
        boolean exists = Boolean.TRUE.equals(searchRepository.existsByTagInfo_ImageUrl(imageUrl));
        if (exists) {
            log.warn(">>> saveTag - 이미 저장된 태그입니다.");
            return;
        }

        for (String tag : tags) {
            // TagInfo 객체 생성
            SearchDocument.TagInfo tagInfo = new SearchDocument.TagInfo(tag, imageUrl);
            // "tags-"로 시작하는 고유 ID를 가지는 SearchDocument 생성
            SearchDocument document = SearchDocument.from(tagInfo);

            searchRepository.save(document); // Elasticsearch에 저장
        }
    }

    @Override
    @Transactional
    public List<String> getImageUrlsByTag(String tag) {
        List<SearchDocument> searches = searchRepository.findByTagInfo_TagContaining(tag);

        // 키워드 조회수 + 1
        Keyword keyword = keywordRepository.findByKeywordName(tag);
        if (keyword != null) {
            keyword.setUseCount(keyword.getUseCount() + 1);
        }

        return searches.stream()
                .map(search -> new String(
                        search.getTagInfo().getImageUrl()
                ))
                .collect(Collectors.toList());
    }

}