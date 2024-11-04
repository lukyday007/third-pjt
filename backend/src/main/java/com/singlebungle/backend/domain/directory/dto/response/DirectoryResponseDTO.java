package com.singlebungle.backend.domain.directory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DirectoryResponseDTO {
    private Long directoryId;
    private String directoryName;
}