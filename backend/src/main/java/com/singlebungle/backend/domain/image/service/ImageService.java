package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import java.util.Map;

public interface ImageService {

    String uploadImageFromUrlToS3(String url);
    void saveImage(Long userId, String sourceUrl, String imageUrl, Long directoryId);

    Map<String, Object> getImageListFromDir(ImageListGetRequestDTO requestDTO);
    Map<String, Object> getImageListFromFeed(ImageListGetRequestDTO requestDTO);

    ImageInfoResponseDTO getImageInfo(Long imageId);
}
