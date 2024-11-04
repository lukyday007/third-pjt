package com.singlebungle.backend.domain.image.dto.response;

import com.singlebungle.backend.domain.image.entity.Image;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title="IMAGE_INFO_RES : 이미지 정보 반환 DTO")
public class ImageInfoResponseDTO {

    @Schema(description = "이미지 id")
    private Long imageId;

    @Schema(description = "이미지 url")
    private String imageUrl;

    @Schema(description = "출처 url")
    private String sourceUrl;

    @Schema(description = "키워드")
    private List<String> keywords;

    public static ImageInfoResponseDTO convertToDTO(Image image, List<String> kws) {
        if (image == null) {
            throw new IllegalArgumentException("image 엔티티의 일부 목록이 null입니다.");
        }

        return ImageInfoResponseDTO.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .sourceUrl(image.getSourceUrl())
                .keywords(kws)
                .build();
    }
}
