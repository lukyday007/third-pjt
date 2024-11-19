package com.singlebungle.backend.domain.directory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "DIR_UPDATE_REQ : 디렉토리명 업데이트 DTO")
public class DirectoryUpdateRequestDTO {
    private Long directoryId;
    private String directoryName;
}