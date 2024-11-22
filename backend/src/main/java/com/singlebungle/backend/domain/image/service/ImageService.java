package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageListFromDirResponseDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ImageService {

    String uploadImageFromUrlToS3(String url);

    InputStream convertWebPToJPG(InputStream webpInputStream) throws IOException;

    String getExtensionFromContentType(String contentType);

    void saveImage(Long userId, String sourceUrl, String imageUrl, Long directoryId);

    Map<String, Object> getImageListFromDir(ImageListGetRequestDTO requestDTO);
    Map<String, Object> getImageListFromFeed(ImageListGetRequestDTO requestDTO);

    ImageInfoResponseDTO getImageInfo(Long imageId);

    void updateKeywordInRedis(String keyword, String currentTime);
}
