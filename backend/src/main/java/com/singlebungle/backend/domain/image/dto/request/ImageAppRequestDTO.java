package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "IMAGE_APP_REQ : 이미지 앱 요청 DTO")
public class ImageAppRequestDTO {

    @NotNull(message = "imageId을 입력해주세요.")
    @Schema(description = "imageId", required = true)
    private Long imageId;

    @Schema(description = "directory id")
    @Builder.Default
    private Long directoryId = 0L;

}