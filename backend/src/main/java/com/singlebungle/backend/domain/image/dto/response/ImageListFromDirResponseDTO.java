package com.singlebungle.backend.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title="IMAGE_LIST_RES : 이미지 목록 반환 DTO")
public class ImageListFromDirResponseDTO {

    private Long imageManagementId;
    private Long imageId;
    private String imageUrl;
    private LocalDateTime createdAt;

    public ImageListFromDirResponseDTO(Long imageManagementId, Long imageId, String imageUrl) {
        this.imageManagementId = imageManagementId;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

}
