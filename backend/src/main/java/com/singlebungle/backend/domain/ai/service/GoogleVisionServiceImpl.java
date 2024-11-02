package com.singlebungle.backend.domain.ai.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.singlebungle.backend.global.exception.InvalidApiUrlException;
import com.singlebungle.backend.global.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleVisionServiceImpl implements GoogleVisionService {

    @Override
    public boolean detectSafeSearchGoogleVision(Image image) throws IOException {

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build())
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format(">>> Error: %s%n", res.getError().getMessage());
                    return false;
                }

                SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
                log.info("Adult: {}, Medical: {}, Spoof: {}, Violence: {}, Racy: {}",
                        annotation.getAdult(), annotation.getMedical(),
                        annotation.getSpoof(), annotation.getViolence(), annotation.getRacy());

                // 'LIKELY' 또는 'VERY_LIKELY'가 2개 이상인지 검사
                int count = 0;
                count += isLikelyOrVeryLikely(annotation.getAdult()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getMedical()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getSpoof()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getViolence()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getRacy()) ? 1 : 0;

                System.out.println("========== count ==========");
                System.out.println(count);

                // 2개 이상일 경우 false 반환
                if (count >= 2) {
                    log.warn(">>> 이미지 안전성 판별 결과 두 개 이상의 항목이 부적절함에 해당됩니다.");
                    return false;
                }
            }
        } catch (WebClientResponseException.Unauthorized e) {
            throw new InvalidApiUrlException(">>> Google Vision api url이 부정확합니다. 확인해주세요.");

        } catch (IOException e) {
            throw new RuntimeException(">>> Google Vision Credential이 제대로 등록되지 않았습니다.", e);

        } catch( IllegalStateException e) {
            throw new RuntimeException(">>> Google Vision Credential이 제대로 등록되지 않았습니다. : " + e);
        }

        return true;
    }

    // Likelihood 값이 LIKELY 또는 VERY_LIKELY인지 확인하는 메서드
    private static boolean isLikelyOrVeryLikely(Likelihood likelihood) {
        return likelihood == Likelihood.LIKELY || likelihood == Likelihood.VERY_LIKELY;
    }

    @Override
    public List<String> analyzeImage(String imageUrl) throws IOException {

        Image image = isBase64Image(imageUrl) ? buildImageFromBase64(imageUrl) : buildImageFromUrl(imageUrl);

        if (!detectSafeSearchGoogleVision(image)) {
            throw new InvalidImageException(">>> Google Vision - 부적절한 이미지 입니다.");
        }

        // detectSafeSearchGoogleVision이 true인 경우에만 실행
        return detectLabels(image);
    }

    @Override
    public boolean isBase64Image(String imageUrl) {
        return imageUrl.startsWith("data:image");
    }

    @Override
    public Image buildImageFromBase64(String base64Image) {
        String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        return Image.newBuilder().setContent(ByteString.copyFrom(decodedBytes)).build();
    }

    @Override
    public Image buildImageFromUrl(String imageUrl) {
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        return Image.newBuilder().setSource(imgSource).build();
    }

    // 외부 이미지 URL을 사용하여 라벨 검출 실행
    @Override
    public List<String> detectLabels(Image image) throws IOException {

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build())
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = response.getResponsesList().get(0);

            if (res.hasError()) {
                System.out.format("Error: %s%n", res.getError().getMessage());
                return null;
            }

            // description 값만 추출하여 리스트로 반환
            return res.getLabelAnnotationsList().stream()
                    .map(EntityAnnotation::getDescription)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error(">>> Google Vision API label request failed: {}", e.getMessage());
            throw new InvalidImageException(">>> Google Vision 부적절한 이미지 입니다. - " + e);
        }
    }
}
