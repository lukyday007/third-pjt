package com.singlebungle.backend.domain.search.service;

import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.search.entity.SearchDocument;
import com.singlebungle.backend.domain.search.repository.SearchRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
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
    private final ImageRepository imageRepository;

    @Override
    public void saveTags(List<String> tags, String imageUrl) {

        Image image = imageRepository.findByImageUrl(imageUrl);

        if (image.isTag()) {
            throw new EntityIsFoundException("이미 존재하는 이미지 데이터 입니다. 태그 생성을 중지합니다.");
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
        System.out.println("여기까지 온 거 맞아?? 마즘???");
        // 키워드가 포함된 문서 목록을 검색
        List<SearchDocument> documents = searchRepository.findByTagInfo_TagContaining(keyword);

        // 로그로 검색된 문서의 태그 확인
        documents.forEach(doc -> {
            String tag = doc.getTagInfo().getTag();
            log.info(">>> Found Tag: {}", tag); // 태그 값을 로그로 출력
        });

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