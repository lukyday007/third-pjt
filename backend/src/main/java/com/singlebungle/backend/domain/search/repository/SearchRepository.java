package com.singlebungle.backend.domain.search.repository;

import com.singlebungle.backend.domain.search.entity.SearchDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String> {

    // tag로 부분 일치하는 문서 목록 조회
    @Query("{ \"bool\": { \"must\": [ { \"match_phrase_prefix\": { \"tagInfo.tag\": \"?0\" } } ] } }")
    List<SearchDocument> findByTagInfo_TagContaining(String tag);



    // imageUrl로 존재 여부 확인
    @Query("{ \"bool\": { \"must\": [ { \"term\": { \"tagInfo.imageUrl\": \"?0\" } } ] } }")
    List<SearchDocument> findDocumentsByTagInfo_ImageUrl(String imageUrl);
}
