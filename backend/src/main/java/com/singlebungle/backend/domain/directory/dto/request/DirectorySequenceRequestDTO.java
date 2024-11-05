package com.singlebungle.backend.domain.directory.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DirectorySequenceRequestDTO {
    private List<Long> directorySequence;
}
