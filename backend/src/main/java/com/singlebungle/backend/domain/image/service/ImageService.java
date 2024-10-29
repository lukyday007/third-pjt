package com.singlebungle.backend.domain.image.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ImageService {
    void saveImage(String url);
}
