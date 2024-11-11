package com.singlebungle.backend.domain.directory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "DIR_SEQ_REQ : 디렉토리 순서 변경 DTO")
public class DirectorySequenceRequestDTO {
    private List<Long> directorySequence;
}
