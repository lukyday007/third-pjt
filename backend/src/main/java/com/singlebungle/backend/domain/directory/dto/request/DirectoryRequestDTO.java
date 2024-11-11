package com.singlebungle.backend.domain.directory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "DIR_REQ : 디렉토리 생성 요청 DTO")
public class DirectoryRequestDTO {
    private String directoryName;
}
