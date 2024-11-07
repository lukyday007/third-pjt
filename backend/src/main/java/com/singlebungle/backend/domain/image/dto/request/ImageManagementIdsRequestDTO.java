package com.singlebungle.backend.domain.image.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ImageManagementIdsRequestDTO {
    private List<Long> imageManagementIds;
}
