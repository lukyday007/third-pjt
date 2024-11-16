package com.singlebungle.backend.domain.ai.dto.response;

import lombok.*;

import java.util.List;

@Getter @Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KeywordAndLabels {
    private List<String> keywords;
    private List<String> labels;
}
