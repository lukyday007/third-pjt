package com.singlebungle.backend.domain.ai.service;

import java.io.IOException;
import java.util.List;

import com.google.cloud.vision.v1.*;

public interface GoogleVisionService {

    ImageAnnotatorClient createVisionClient() throws IOException;

    boolean detectSafeSearchGoogleVision(Image image) throws IOException;

    Image buildImage(String imageUrl);

    boolean isUrlAccessible(String imageUrl);

    Image buildImageFromUrlDirect(String imageUrl);

    Image buildImageFromUrlFallback(String imageUrl);

    Image buildImageFromBase64(String imageUrl);

    Image buildImageFromWebp(byte[] webpBytes);

    List<String> analyzeImage(String imageUrl) throws IOException;

    List<String> detectLabels(Image image) throws IOException;

}
