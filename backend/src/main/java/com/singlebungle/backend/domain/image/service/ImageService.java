package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ImageService {

    void uploadImageFromUrlToS3(String url);
    void saveImage(String imageUrl, String webUrl, Long directoryId);

}
