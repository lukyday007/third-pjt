package com.singlebungle.backend.domain.image.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveImagesRequestDTO {
    private List<Long> imageManagementIds; // Image ID 대신 ImageManagement ID로 변경
    private Long directoryId;
}
