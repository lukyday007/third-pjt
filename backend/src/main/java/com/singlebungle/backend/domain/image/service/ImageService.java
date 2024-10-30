package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import org.springframework.stereotype.Service;

@Service
public interface ImageService {

    void uploadImageFromUrlToS3(String url);
    void saveImage(ImageWebRequestDTO requestDTO);
}
