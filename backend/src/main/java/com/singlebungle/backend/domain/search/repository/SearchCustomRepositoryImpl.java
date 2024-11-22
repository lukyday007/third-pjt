package com.singlebungle.backend.domain.search.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.singlebungle.backend.domain.search.entity.SearchDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Repository
public class SearchCustomRepositoryImpl implements SearchCustomRepository {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public List<SearchDocument> searchByWildcardAndBoost(String tag) {
        try {
            // Wildcard Query
            WildcardQuery wildcardQuery = QueryBuilders.wildcard()
                    .field("tagInfo.tag")
                    .value("*" + tag + "*")
                    .build();

            // Prefix Query
            PrefixQuery prefixQuery = QueryBuilders.prefix()
                    .field("tagInfo.tag")
                    .value(tag)
                    .build();

            // Term Query
            TermQuery termQuery = QueryBuilders.term()
                    .field("tagInfo.tag.keyword")
                    .value(tag)
                    .build();

            // Script for Length Boost
            ScriptScoreFunction scriptScore = ScriptScoreFunction.of(ss -> ss
                    .script(sc -> sc
                            .inline(i -> i
                                    .source("1.0 / params['_source']['tagInfo']['tag'].length()")) // 길이에 반비례
                    )
            );

            // Function Score Query
            FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery.Builder()
                    .query(wildcardQuery._toQuery()) // 기본 쿼리
                    .functions(f -> f
                            .filter(prefixQuery._toQuery()) // Prefix Boost
                            .weight(20.0))
                    .functions(f -> f
                            .filter(termQuery._toQuery()) // Term Boost
                            .weight(50.0))
                    .functions(f -> f
                            .scriptScore(scriptScore)) // 길이 가중치
                    .boostMode(FunctionBoostMode.Sum) // 점수 합산
                    .build();

            // Search Request 생성
            SearchRequest request = SearchRequest.of(s -> s
                    .index("tags")
                    .query(functionScoreQuery._toQuery())
                    .sort(SortOptions.of(so -> so
                            .field(f -> f
                                    .field("_score")
                                    .order(SortOrder.Desc))))
                    .source(src -> src
                            .filter(f -> f
                                    .includes("id", "tagInfo.tag", "tagInfo.imageUrl"))) // 포함할 필드 지정
            );

            // Elasticsearch 쿼리 실행
            SearchResponse<SearchDocument> response = elasticsearchClient.search(request, SearchDocument.class);

            // 결과 반환
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch query execution failed", e);
        }
    }
}