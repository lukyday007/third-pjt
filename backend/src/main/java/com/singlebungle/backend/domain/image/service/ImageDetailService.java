package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;

import java.util.List;

public interface ImageDetailService {
    void saveImageDetail(ImageWebRequestDTO requestDTO, List<String> keywords);
}
