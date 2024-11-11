package com.singlebungle.backend.domain.directory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(title = "DIR_LIST_RES : 디렉토리 목록 반환 DTO")
public class DirectoryListResponseDTO {
    private List<DirectoryResponseDTO> directories;
}
