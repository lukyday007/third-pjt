package com.singlebungle.backend.domain.ai.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.singlebungle.backend.global.config.GoogleVisionConfig;
import com.singlebungle.backend.global.exception.InvalidApiUrlException;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.exception.InvalidRequestException;
import com.singlebungle.backend.global.exception.UrlAccessException;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
                count += isLikelyOrVeryLikely(annotation.getViolence()) ? 1 : 0;
                count += isLikelyOrVeryLikely(annotation.getRacy()) ? 1 : 0;

                log.info("Adult: {}, Medical: {}, Spoof: {}, Violence: {}, Racy: {}", annotation.getAdult(), annotation.getMedical(), annotation.getSpoof(), annotation.getViolence(), annotation.getRacy());

                // 2개 이상일 경우 false 반환
                if (count >= 2) {
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

        // 병렬 처리 시작
        CompletableFuture<Boolean> safeSearchFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return detectSafeSearchGoogleVision(image);
            } catch (IOException e) {
                log.error(">>> SafeSearch 검증 중 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("SafeSearch 검증 실패", e);
            }
        });

        CompletableFuture<List<String>> labelsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return detectLabels(image);
            } catch (IOException e) {
                log.error(">>> 라벨 검출 중 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("라벨 검출 실패", e);
            }
        });

        // 결과 병합
        try {
            boolean isSafe = safeSearchFuture.get(); // SafeSearch 결과
            if (!isSafe) {
                throw new InvalidImageException(">>> Google Vision - 부적절한 이미지 입니다.");
            }
            return labelsFuture.get(); // 라벨 검출 결과
        } catch (Exception e) {
            log.error(">>> 이미지 분석 병렬 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 분석 병렬 처리 실패", e);
        }

    }


    @Override
    public Image buildImage(String imageUrl) {
        try {
            if (imageUrl.startsWith("data:image")) {
                return buildImageFromBase64(imageUrl);

            } else if (imageUrl.endsWith(".webp")) {
                log.info(">>> WebP 이미지 감지, 변환을 시도합니다. URL: {}", imageUrl);

                // WebP 지원 여부 확인
                if (!isWebpSupported()) {
                    throw new UnsupportedOperationException("WebP 이미지 처리가 지원되지 않습니다. 라이브러리를 확인하세요.");
                }

                return buildImageFromWebp(new URL(imageUrl).openStream().readAllBytes());

            } else {
                if (isUrlAccessible(imageUrl)) {
                    log.info(">>> 일반 URL 이미지 처리 시작...");
                    try {
                        return buildImageFromUrlDirect(imageUrl);
                    } catch (Exception e) {
                        log.warn(">>> URL 직접 접근 실패, 바이너리로 전환: {}", imageUrl);
                        return buildImageFromUrlFallback(imageUrl);
                    }
                } else {
                    throw new UrlAccessException("해당 이미지 URL에 접근할 수 없습니다: " + imageUrl);
                }
            }
        } catch (IOException e) {
            log.error(">>> 이미지 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }


    // URL 접근 가능 여부 확인
    private boolean isUrlAccessible(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // HEAD 요청으로 리소스 확인
            connection.setInstanceFollowRedirects(true); // 리다이렉션 허용
            connection.setConnectTimeout(3000); // 연결 제한 시간 3초
            connection.setReadTimeout(3000); // 읽기 제한 시간 3초
            int responseCode = connection.getResponseCode();

            log.info(">>> URL 접근 테스트: {} - 응답 코드: {}", imageUrl, responseCode);

            // 리다이렉션 및 성공 상태 코드 처리
            return (responseCode == HttpURLConnection.HTTP_OK ||
                    (responseCode >= 300 && responseCode < 400));
        } catch (Exception e) {
            log.error(">>> URL 접근 실패: {} - 예외 메시지: {}", imageUrl, e.getMessage(), e);
            return false;
        }
    }

    // URL 직접 사용
    private Image buildImageFromUrlDirect(String imageUrl) {
//        log.info(">>> URL을 통해 직접 이미지 사용: {}", imageUrl);
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();

        return Image.newBuilder().setSource(imgSource).build();
    }

    private Image buildImageFromUrlFallback(String imageUrl) {
        log.info(">>> URL 접근 불가, 바이너리 데이터로 다운로드 처리: {}", imageUrl);
        try {
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            byte[] imageBytes = inputStream.readAllBytes();
            log.info(">>> URL을 통해 이미지 다운로드 성공: {} (크기: {} 바이트)", imageUrl, imageBytes.length);
            return Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build();

        } catch (IOException e) {
            log.error(">>> URL을 통해 이미지를 다운로드할 수 없습니다: {}", imageUrl, e);
            throw new RuntimeException("URL을 통해 이미지를 다운로드하는 동안 오류가 발생했습니다.", e);
        }
    }


    // Base64 이미지 처리
    private Image buildImageFromBase64(String imageUrl) {
        String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);

        if (imageUrl.startsWith("data:image/webp")) {
            log.info(">>> WebP(Base64) 감지, JPG로 변환 중...");
            return buildImageFromWebp(decodedBytes);
        }
        log.info("=== Base64을 통한 이미지 변환중 ===>");
        return Image.newBuilder().setContent(ByteString.copyFrom(decodedBytes)).build();
    }


    private boolean isWebpSupported() {
        boolean isSupported = ImageIO.getImageReadersByFormatName("webp").hasNext();
        log.info(">>> WebP 지원 여부: {}", isSupported);
        return isSupported;
    }


    // WebP 이미지 처리
    @Override
    public Image buildImageFromWebp(byte[] webpBytes) {
        try {
            // WebP 이미지를 BufferedImage로 읽음
            InputStream webpInputStream = new ByteArrayInputStream(webpBytes);
            BufferedImage bufferedImage = ImageIO.read(webpInputStream);

            // 이미지 읽기 검증
            if (bufferedImage == null) {
                log.error(">>> WebP 이미지를 읽을 수 없습니다. WebP 파일이 손상되었거나 지원되지 않는 형식입니다.");
                throw new IllegalArgumentException("WebP 이미지를 읽는 데 실패했습니다.");
            }

            // WebP -> JPG 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);

            // 변환된 이미지 크기 검증
            byte[] jpgBytes = outputStream.toByteArray();
            if (jpgBytes.length == 0) {
                log.error(">>> WebP 변환 실패: 결과 이미지 데이터가 비어 있습니다.");
                throw new RuntimeException("WebP 변환 실패: 결과 이미지 데이터가 비어 있습니다.");
            }

            log.info(">>> WebP 이미지를 JPG로 성공적으로 변환했습니다. 변환된 이미지 크기: {} bytes", jpgBytes.length);

            // Google Vision API와 호환되는 Image 객체 반환
            return Image.newBuilder().setContent(ByteString.copyFrom(jpgBytes)).build();

        } catch (IOException e) {
            log.error(">>> WebP 이미지를 JPG로 변환하는 동안 IOException 발생: {}", e.getMessage(), e);
            throw new RuntimeException("WebP 이미지를 JPG로 변환하는 동안 오류가 발생했습니다.", e);
        }
    }


    // 외부 이미지 URL을 사용하여 라벨 검출 실행
    private List<String> detectLabels(Image image) throws IOException {

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build())
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = createVisionClient()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            AnnotateImageResponse res = response.getResponsesList().get(0);

            if (res.hasError()) {
                log.error(">>> Google Vision API 라벨 검출 실패: {}", res.getError().getMessage());
                throw new InvalidImageException("Google Vision - 라벨 검출 실패: " + res.getError().getMessage());
            }

//            // 라벨 검출 결과 로그 출력
//            log.info(">>> Google Vision API 라벨 응답: {}", res.getLabelAnnotationsList());

            // description 값만 추출하여 리스트로 반환
            return res.getLabelAnnotationsList().stream()
                    .map(EntityAnnotation::getDescription)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error(">>> Google Vision API 라벨 요청 중 오류 발생: {}", e.getMessage(), e);
            throw new InvalidImageException("Google Vision - 라벨 요청 중 오류 발생 : "+ e);
        }
    }


}