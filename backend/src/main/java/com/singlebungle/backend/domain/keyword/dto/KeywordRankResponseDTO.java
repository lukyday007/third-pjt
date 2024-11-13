package com.singlebungle.backend.domain.keyword.dto;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeywordRankResponseDTO {

    private String keyword;

    // "up", "down", "same" 상태
    private String isState;

}
