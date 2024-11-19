package com.singlebungle.backend.domain.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Schema(title="IMAGE_DELETE_REQ : 이미지 삭제 요청DTO")
public class ImageIdDeleteRequestDTO {

    private List<Long> imageManagementIds;

}
