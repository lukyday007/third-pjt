package com.singlebungle.backend.domain.search.repository;

import org.springframework.data.elasticsearch.annotations.Query;
import com.singlebungle.backend.domain.search.entity.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.List;

@EnableElasticsearchRepositories
public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String> {

    // imageUrl로 존재 여부 확인
    @Query("{ \"bool\": { \"must\": [ { \"term\": { \"tagInfo.imageUrl\": \"?0\" } } ] } }")
    Boolean  existsByTagInfo_ImageUrl(String imageUrl);

    // tag로 부분 일치하는 문서 목록 조회
    @Query("{ \"bool\": { \"must\": [ { \"match_phrase_prefix\": { \"tagInfo.tag\": \"?0\" } } ] } }")
    List<SearchDocument> findByTagInfo_TagContaining(String tag);
}
