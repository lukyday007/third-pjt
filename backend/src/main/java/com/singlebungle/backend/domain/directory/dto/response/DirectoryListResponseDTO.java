package com.singlebungle.backend.domain.directory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DirectoryListResponseDTO {
    private List<DirectoryResponseDTO> directories;
}
