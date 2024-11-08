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

        List<SearchDocument> imageUrlList = searchRepository.findDocumentsByTagInfo_ImageUrl(imageUrl);

        // imageUrl 기준으로 중복 여부 체크
        if (imageUrlList != null || !imageUrlList.isEmpty()) {
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
    public List<String> getKeywordsByTag(String keyword) {
        // 키워드가 포함된 문서 목록을 검색
        List<SearchDocument> documents = searchRepository.findByTagInfo_TagContaining(keyword);

        // 검색된 문서에서 태그만 추출하여 리스트로 반환
        return documents.stream()
                .map(doc -> doc.getTagInfo().getTag())  // TagInfo에서 tag 값을 추출
                .distinct()  // 중복 태그
                .collect(Collectors.toList());
    }

    @Override
    public void incrementSearchCount(String keyword) {

    }

}