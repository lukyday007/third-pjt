package com.singlebungle.backend.domain.keyword.entity;

import com.singlebungle.backend.global.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "keyword")
public class Keyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", unique = true, nullable = false)
    private Long keywordId;

    @Column(name = "keyword_name", unique = true, nullable = false)
    private String keywordName;

    @Column(name = "use_count")
    private int useCount = 0;

    // createdAt
    // updatedAt

    public static Keyword convertToEntity(String keyword) {
        Keyword kw = new Keyword();
        kw.setKeywordName(keyword);

        return kw;
    }

}