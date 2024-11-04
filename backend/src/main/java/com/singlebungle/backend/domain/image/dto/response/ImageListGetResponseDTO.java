package com.singlebungle.backend.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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

}
