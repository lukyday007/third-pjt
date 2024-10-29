package com.singlebungle.backend.domain.ai.service;

import com.google.cloud.vision.v1.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleVisionServiceImpl implements GoogleVisionService {

    @Override
    public boolean detectSafeSearchGoogleVision(String imageUrl) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        // 외부 이미지 URL을 직접 사용하도록 구성
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();

        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return false;
                }

                SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();

                // 'LIKELY' 또는 'VERY_LIKELY'가 2개 이상인지 검사
                int count = 0;
                count += isLikelyOrVeryLikely(annotation.getAdult()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getMedical()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getSpoof()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getViolence()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getRacy()) ? 1 : 0;

                // 2개 이상일 경우 false 반환
                if (count >= 2)
                return false;
            }
        }

        return true;
    }

    @Override
    public List<String> analyzeImage(String imageUrl) throws IOException {
        if (!detectSafeSearchGoogleVision(imageUrl)) {
            throw new RuntimeException("부적절한 이미지입니다.");
        }

        // detectSafeSearchGoogleVision이 true인 경우에만 실행
        return detectLabels(imageUrl);
    }


    // Likelihood 값이 LIKELY 또는 VERY_LIKELY인지 확인하는 메서드
    private static boolean isLikelyOrVeryLikely(Likelihood likelihood) {
        return likelihood == Likelihood.LIKELY || likelihood == Likelihood.VERY_LIKELY;
    }


    // 외부 이미지 URL을 사용하여 라벨 검출 실행
    @Override
    public List<String> detectLabels(String imageUrl) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        Image img = Image.newBuilder().setSource(imgSource).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            AnnotateImageResponse res = response.getResponsesList().get(0);

            if (res.hasError()) {
                System.out.format("Error: %s%n", res.getError().getMessage());
                return new ArrayList<>();
            }

            // description 값만 추출하여 리스트로 반환
            return res.getLabelAnnotationsList().stream()
                    .map(EntityAnnotation::getDescription)
                    .collect(Collectors.toList());
        }
    }

}
