package com.singlebungle.backend.domain.keyword.dto;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KeywordRankResponseDTO {

    private String keyword;

    // "up", "down", "same" 상태
    private String isState;

    private Double gap;

    public KeywordRankResponseDTO(String keyword, String isState) {
        this.keyword = keyword;
        this.isState = isState;
    }
}
