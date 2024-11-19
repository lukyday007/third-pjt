package com.singlebungle.backend.domain.ai.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GoogleVisionResponseDTO {

    private List<LabelAnnotation> labelAnnotations;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class LabelAnnotation {
        private String description;
        private float score;

    }

}
