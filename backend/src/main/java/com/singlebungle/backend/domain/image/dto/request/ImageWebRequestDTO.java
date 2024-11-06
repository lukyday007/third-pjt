package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(title = "IMAGE_WEB_REQ : 이미지 웹 요청 DTO")
public class ImageWebRequestDTO {

    @NotNull(message = "web url을 입력해주세요.")
    @Schema(description = "web url")
    private String webUrl;

    @NotNull(message = "image url을 입력해주세요.")
    @Schema(description = "image url")
    private String imageUrl;

    @NotNull(message = "directoryId를 입력해주세요.")
    @Builder.Default
    private Long directoryId = 0L;

}