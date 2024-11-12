package com.singlebungle.backend.domain.ai.service;

import java.io.IOException;
import java.util.List;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.singlebungle.backend.global.config.GoogleVisionConfig;


public interface GoogleVisionService {

    ImageAnnotatorClient createVisionClient() throws IOException;

    boolean detectSafeSearchGoogleVision(Image image) throws IOException;

    List<String> detectLabels(Image image) throws IOException;

    Image buildImage(String imageUrl);

    Image buildImageFromWebp(byte[] webpBytes);

    List<String> analyzeImage(String imageUrl) throws IOException;

}
