package com.singlebungle.backend.domain.ai.service;

import java.io.IOException;
import java.util.List;

public interface GoogleVisionService {
    boolean detectSafeSearchGoogleVision(String gcsPath) throws IOException;

    List<String> detectLabels(String imageUrl) throws IOException;

    List<String> analyzeImage(String imageUrl) throws IOException;

}
