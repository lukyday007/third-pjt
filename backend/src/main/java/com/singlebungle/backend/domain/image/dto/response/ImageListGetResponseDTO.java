package com.singlebungle.backend.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title="IMAGE_INFO_RES : 이미지 정보 DTO")
public class ImageListGetResponseDTO {

    private Long imageId;
    private String imageUrl;
    private List<String> keywords;
    private String webUrl;

}
