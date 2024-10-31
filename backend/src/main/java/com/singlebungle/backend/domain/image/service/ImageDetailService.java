package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;

import java.util.List;

public interface ImageDetailService {
    void saveImageDetail(String webUrl, String imageUrl, List<String> keywords);
}
