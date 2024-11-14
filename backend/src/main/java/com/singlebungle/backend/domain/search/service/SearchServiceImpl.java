package com.singlebungle.backend.domain.search.service;

import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.search.entity.SearchDocument;
import com.singlebungle.backend.domain.search.repository.SearchCustomRepository;
import com.singlebungle.backend.domain.search.repository.SearchRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchRepository searchRepository;
    private final SearchCustomRepository searchCustomRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
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

        image.setTag(true);
        imageRepository.save(image);
    }


    @Override
    public List<String> getKeywordsByTag(String keyword) {
        // 키워드가 포함된 문서 목록을 검색
//        List<SearchDocument> documents = searchRepository.findByTagInfoContainingTag(keyword);
        List<SearchDocument> documents = searchCustomRepository.searchByWildcardAndBoost(keyword);

//        List<SearchDocument> documents = searchRepository.findByTagInfoTag(keyword);

        // 검색된 문서에서 태그만 추출하여 리스트로 반환
        return documents.stream()
                .map(doc -> doc.getTagInfo().getTag())  // TagInfo에서 tag 값을 추출
                .distinct()  // 중복 태그
                .collect(Collectors.toList());
    }


//    @Autowired
//    private ElasticsearchOperations elasticsearchOperations;
//
//    public List<String> getKeywordsByTag(String keyword) {
//        // Criteria를 사용하여 쿼리 구성
//        Criteria criteria = new Criteria("tagInfo.tag")
//                .contains(keyword) // 부분 일치 검색
//                .boost(10);        // 가중치 추가
//
//        CriteriaQuery query = new CriteriaQuery(criteria);
//        query.addSort(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("_score")));
//
//        // ElasticsearchOperations로 검색 수행
//        SearchHits<SearchDocument> searchHits = elasticsearchOperations.search(query, SearchDocument.class);
//
//        // 결과 태그 리스트로 반환
//        return searchHits.stream()
//                .map(hit -> hit.getContent().getTagInfo().getTag())  // 태그 추출
//                .distinct()  // 중복 제거
//                .collect(Collectors.toList());
//    }
//


    @Override
    public void incrementSearchCount(String keyword) {

    }

}
