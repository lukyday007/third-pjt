package com.singlebungle.backend.domain.search.repository;

import com.singlebungle.backend.domain.search.entity.SearchDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String>, SearchCustomRepository  {

    // imageUrl로 존재 여부 확인
    @Query("{ \"bool\": { \"must\": [ { \"term\": { \"tagInfo.imageUrl\": \"?0\" } } ] } }")
    List<SearchDocument> findDocumentsByTagInfo_ImageUrl(String imageUrl);
}
