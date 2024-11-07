package com.singlebungle.backend.domain.image.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveImagesRequestDTO {
    private List<Long> imageIds;
    private Long directoryId;
}