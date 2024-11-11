package com.singlebungle.backend.domain.image.dto.response;

import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.global.model.BaseTimeEntity;
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
public class ImageListGetResponseDTO {

    private Long imageId;
    private String imageUrl;
    private LocalDateTime createdAt;

    public ImageListGetResponseDTO(Long imageId, String imageUrl) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

}
