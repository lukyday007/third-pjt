package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Schema(title = "IMAGE_APP_REQ : 이미지 앱 요청 DTO")
public class ImageAppRequestDTO {

    @NotNull(message = "imageId을 입력해주세요.")
    @Schema(description = "imageId", required = true)
    private Long imageId;

//    @NotNull(message = "directoryId를 입력해주세요.")
    @Schema(description = "directoryId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long directoryId;

}