package com.singlebungle.backend.domain.directory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(title = "DIR_RES : 디렉토리 정보 반환 DTO")
public class DirectoryResponseDTO {
    private Long directoryId;
    private String directoryName;
}