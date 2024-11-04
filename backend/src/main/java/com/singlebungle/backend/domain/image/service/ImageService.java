package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


public interface ImageService {

    String uploadImageFromUrlToS3(String url);
    void saveImage(String sourceUrl, String imageUrl, Long directoryId);

    Map<String, Object> getImageList(ImageListGetRequestDTO requestDTO);

    ImageInfoResponseDTO getImageInfo(Long imageId);
}
