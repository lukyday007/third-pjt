package com.singlebungle.backend.domain.search.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.annotation.Id;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Document(indexName = "tags")
public class SearchDocument {

    @Id
    private String id;

    private TagInfo tagInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {
        private String tag;
        private String imageUrl;
    }

    public static SearchDocument from(TagInfo tagInfo) {
        return SearchDocument.builder()
                .id("tags-" + UUID.randomUUID().toString()) // "tags-"로 시작하는 고유 ID 생성
                .tagInfo(tagInfo)
                .build();
    }

}
