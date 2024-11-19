package com.singlebungle.backend.domain.ai.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KeywordsFromOpenAi {
    private List<String> keywords;

}
