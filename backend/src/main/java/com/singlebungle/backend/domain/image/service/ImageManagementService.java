package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;

public interface ImageManagementService {
    void saveImageManagement(Long userId, Long imageId, Long directoryId);

    void deleteImages(ImageIdDeleteRequestDTO requestDTO);
}
