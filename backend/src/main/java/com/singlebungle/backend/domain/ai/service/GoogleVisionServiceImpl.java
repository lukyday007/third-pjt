package com.singlebungle.backend.domain.ai.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.singlebungle.backend.global.config.GoogleVisionConfig;
import com.singlebungle.backend.global.exception.InvalidApiUrlException;
import com.singlebungle.backend.global.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleVisionServiceImpl implements GoogleVisionService {

    private final GoogleVisionConfig googleVisionConfig;

    @Override
    public ImageAnnotatorClient createVisionClient() throws IOException {
        GoogleCredentials credentials = googleVisionConfig.getGoogleCredentials();
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

        return ImageAnnotatorClient.create(settings);
    }

    @Override
    public boolean detectSafeSearchGoogleVision(Image image) throws IOException {

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build())
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = createVisionClient()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format(">>> Error: %s%n", res.getError().getMessage());
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
                if (count >= 2) {
                    log.info("Adult: {}, Medical: {}, Spoof: {}, Violence: {}, Racy: {}",
                            annotation.getAdult(), annotation.getMedical(),
                            annotation.getSpoof(), annotation.getViolence(), annotation.getRacy());
                    log.warn(">>> 이미지 안전성 판별 결과 두 개 이상의 항목이 부적절함에 해당됩니다. : " + count);
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
        // 단일 처리 메서드 사용
        Image image = buildImage(imageUrl);

        if (!detectSafeSearchGoogleVision(image)) {
            throw new InvalidImageException(">>> Google Vision - 부적절한 이미지 입니다.");
        }

        return detectLabels(image);
    }

    @Override
    public Image buildImage(String imageUrl) {
        try {
            // Base64 이미지 처리
            if (imageUrl.startsWith("data:image")) {
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);

                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

                    // Base64가 WebP 형식인지 판별 후 변환
                    if (imageUrl.startsWith("data:image/webp")) {
                        log.info(">>> WebP(Base64) 감지, JPG로 변환 중...");
                        return buildImageFromWebp(decodedBytes);
                    }
                    // 일반 Base64 이미지 처리
                    return Image.newBuilder().setContent(ByteString.copyFrom(decodedBytes)).build();

                } catch (IllegalArgumentException e) {
                    log.error(">>> Base64 데이터 디코딩 실패: {}", e.getMessage());
                    throw new RuntimeException(">>> Base64 데이터를 디코딩하는 동안 오류가 발생했습니다.", e);
                }
            }

            // WebP URL 처리
            if (imageUrl.endsWith(".webp")) {
                log.info(">>> WebP(URL) 감지, JPG로 변환 중...");
                byte[] webpBytes = new URL(imageUrl).openStream().readAllBytes();

                return buildImageFromWebp(webpBytes);
            }

            // 일반 URL 이미지 처리
            ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
            return Image.newBuilder().setSource(imgSource).build();

        } catch (IOException e) {
            throw new RuntimeException(">>> 이미지를 처리하는 동안 오류가 발생했습니다.", e);
        }
    }


    @Override
    public Image buildImageFromWebp(byte[] webpBytes) {
        try {
            InputStream webpInputStream = new ByteArrayInputStream(webpBytes);
            BufferedImage bufferedImage = ImageIO.read(webpInputStream);

            if (bufferedImage == null) {
                log.error(">>> WebP 이미지를 읽을 수 없습니다. WebP 파일이 손상되었거나 지원되지 않는 형식입니다.");
                throw new IllegalArgumentException("WebP 이미지를 읽는 데 실패했습니다.");
            }

            ByteArrayOutputStream jpgOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", jpgOutputStream);
            byte[] jpgBytes = jpgOutputStream.toByteArray();

            if (jpgBytes.length == 0) {
                log.error(">>> 변환된 JPG 이미지 데이터가 비어 있습니다.");
                throw new RuntimeException(">>> JPG 변환 결과가 비어 있습니다.");
            }

            return Image.newBuilder().setContent(ByteString.copyFrom(jpgBytes)).build();

        } catch (IOException e) {
            log.error(">>> WebP 이미지를 JPG로 변환하는 동안 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException(">>> WebP 이미지를 JPG로 변환하는 동안 오류가 발생했습니다.", e);
        }
    }


    // 외부 이미지 URL을 사용하여 라벨 검출 실행
    @Override
    public List<String> detectLabels(Image image) throws IOException {

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build())
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = createVisionClient()) {
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