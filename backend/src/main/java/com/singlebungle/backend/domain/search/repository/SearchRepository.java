//package com.singlebungle.backend.domain.search.repository;
//
//import com.singlebungle.backend.domain.search.entity.SearchDocument;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
//
//import java.util.List;
//
//@EnableElasticsearchRepositories
//public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String> {
//
//    boolean existsByTagInfo_ImageUrl(String imageUrl);
//
//    List<SearchDocument> findByTagInfo_TagContaining(String tag);
//}
//
