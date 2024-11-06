package com.singlebungle.backend.domain.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordAndLabels {
    private List<String> keywords;
    private List<String> tags;
}
