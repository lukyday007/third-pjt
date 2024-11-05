package com.singlebungle.backend.domain.directory.dto.request;

import lombok.Data;

@Data
public class DirectoryUpdateRequestDTO {
    private Long directoryId;
    private String directoryName;
}