package com.singlebungle.backend.domain.image.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageDetail;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.repository.*;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageRepositorySupport imageRepositorySupport;
    private final ImageDetailRepository imageDetailRepository;
    private final ImageManagementRepository imageManagementRepository;
    private final ImageManagementRepositorySupport imageManagementRepositorySupport;
    private final DirectoryRepository directoryRepository;
    private final KeywordService keywordService;
    private final KeywordRepository keywordRepository;
    private final AmazonS3 amazonS3;

    private final String bucketName = "sgbgbucket";

    @Override
    @Transactional
    public String uploadImageFromUrlToS3(String imageUrl) {
        String fileName;

        try {
            InputStream inputStream;
            String contentType;

            if (imageUrl.startsWith("data:image")) {
                // Base64 이미지 처리
                int commaIndex = imageUrl.indexOf(",");
                contentType = imageUrl.substring(5, commaIndex).split(";")[0];
                byte[] imageData = Base64.getDecoder().decode(imageUrl.substring(commaIndex + 1));
                inputStream = new ByteArrayInputStream(imageData);
            } else {
                // URL 이미지 처리
                URL url = new URL(imageUrl);
                URLConnection urlConnection = url.openConnection();
                contentType = urlConnection.getContentType();
                inputStream = urlConnection.getInputStream();
            }

            // WebP 처리
            if ("image/webp".equals(contentType)) {
                inputStream = convertWebPToJPG(inputStream);
                contentType = "image/jpeg";
            }

            // 데이터 길이 계산
            byte[] data = inputStream.readAllBytes();
            long contentLength = data.length;

            // S3 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);

            // 파일명 생성
            String extension = getExtensionFromContentType(contentType);
            fileName = UUID.randomUUID().toString() + extension;

            // S3 업로드
            InputStream uploadStream = new ByteArrayInputStream(data);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, uploadStream, metadata);
            amazonS3.putObject(request);

            log.info(">>> S3에 이미지 업로드 성공: {}", fileName);
            return fileName;

        } catch (IOException e) {
            log.error(">>> URL 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("URL 처리 중 오류가 발생했습니다.", e);
        } catch (AmazonServiceException e) {
            log.error(">>> S3 서비스 오류: {}", e.getMessage(), e);
            throw new RuntimeException("S3 서비스 오류가 발생했습니다.", e);
        } catch (SdkClientException e) {
            log.error(">>> S3 클라이언트 오류: {}", e.getMessage(), e);
            throw new RuntimeException("S3 클라이언트 오류가 발생했습니다.", e);
        }
    }


    private long calculateContentLength(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.size();
    }


    private InputStream convertWebPToJPG(InputStream webpInputStream) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(webpInputStream)) {
            BufferedImage webpImage = ImageIO.read(bufferedInputStream); // WebP 이미지 읽기
            if (webpImage == null) {
                log.error(">>> WebP 이미지를 읽을 수 없습니다. 스트림 확인 필요.");
                throw new IllegalArgumentException("WebP 이미지를 읽는 데 실패했습니다.");
            }

            // WebP 이미지를 JPG로 변환
            ByteArrayOutputStream jpgOutputStream = new ByteArrayOutputStream();
            ImageIO.write(webpImage, "jpg", jpgOutputStream);
            return new ByteArrayInputStream(jpgOutputStream.toByteArray());
        } catch (Exception e) {
            log.error(">>> WebP 이미지를 JPG로 변환하는 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("WebP 이미지를 변환하는 동안 오류가 발생했습니다.", e);
        }
    }


    public String getExtensionFromContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".jpg";
            default:
                throw new IllegalArgumentException("지원하지 않는 이미지 형식: " + contentType);
        }
    }


    @Override
    @Transactional
    public void saveImage(Long userId, String sourceUrl, String imageUrl, Long directoryId) {
        // user
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당하는 유저 데이터가 존재하지 않습니다. :" + userId));

        // image
        boolean isImage = imageRepository.existsByImageUrl(imageUrl);
        if (isImage) {
            throw new EntityIsFoundException("이미 해당 이미지 데이터가 존재합니다");
        }
        Image image = Image.convertToEntity(sourceUrl, imageUrl);
        imageRepository.save(image);

        // directory => 디폴트 일 때 처리
        Directory directory;
        if (directoryId == 0) {
            int status = 0;
            directory =  directoryRepository.findByUserAndStatus(user, status)
                    .orElseThrow(() -> new IllegalStateException("디렉토리가 존재하지 않습니다."));;
        } else {
            directory = directoryRepository.findById(directoryId).orElseThrow(() -> new EntityNotFoundException("해당 디렉토리 데이터가 존재하지 않습니다. " + directoryId));
        }

        ImageManagement imageManagement = ImageManagement.convertToEntity(user, image, directory);
        imageManagementRepository.save(imageManagement);
    }


    @Override
    public Map<String, Object> getImageListFromDir(ImageListGetRequestDTO requestDTO) {

        if (requestDTO.getKeywords() != null || ! requestDTO.getKeywords().isEmpty()) {
            for (String keyword : requestDTO.getKeywords()) {
                keywordService.increaseCurCnt(keyword);
            }
        }

        return imageManagementRepositorySupport.findImageListFromDir(requestDTO);
    }


    @Override
    public Map<String, Object> getImageListFromFeed(ImageListGetRequestDTO requestDTO) {

        if (requestDTO.getKeywords() != null || requestDTO.getKeywords().isEmpty()) {
            for (String keyword : requestDTO.getKeywords()) {
                keywordService.increaseCurCnt(keyword);
            }
        }

        return imageRepositorySupport.findImageListFromFeed(requestDTO);
    }


    @Qualifier("redisKeywordTemplate")
    private final RedisTemplate<String, Object> redisKeywordTemplate;

    @Override
    public ImageInfoResponseDTO getImageInfo(Long imageId) {

        Image image = imageRepository.findById(imageId).orElseThrow(() -> new EntityNotFoundException("일치하는 이미지 데이터가 존재하지 않습니다."));

        List<String> keywordToStr = imageDetailRepository.findAllByImage(image)
                .stream()
                .map(ImageDetail::getKeyword)   // ImageDetail에서 Keyword 엔티티 추출
                .map(Keyword::getKeywordName)   // Keyword 엔티티에서 keywordName 추출
                .collect(Collectors.toList());

        /*
         todo 이미지가 상세조회되면 관련된 키워드의 cnt + 1
         레디스에 반영
        */
        List<String> keywordList = imageManagementRepositorySupport.findKeywordList(image);

        // Redis에 키워드 조회수 반영
        if (keywordList != null && ! keywordList.isEmpty()) {
            for (String keyword: keywordList) {

                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));

                updateKeywordInRedis(keyword, currentTime);

            }
        }

        return ImageInfoResponseDTO.convertToDTO(image, keywordToStr);
    }

    // Redis 키워드 업데이트 로직
    private void updateKeywordInRedis(String keyword, String currentTime) {
        String curCntKey = getRedisKey(keyword, "curCnt");
        String prevCntKey = getRedisKey(keyword, "prevCnt");
        String updatedKey = getRedisKey(keyword, "updated");

        // 현재 조회수 가져오기
        Object currentCountObj = redisKeywordTemplate.opsForHash().get("keyword", curCntKey);
        String currentCountStr = currentCountObj != null ? currentCountObj.toString() : "0";

        // 조회수 증가 (문자형 처리)
        int newCount = Integer.parseInt(currentCountStr) + 1;
        redisKeywordTemplate.opsForHash().put("keyword", curCntKey, String.valueOf(newCount));

        // prevCnt가 없으면 초기값 설정
        if (!redisKeywordTemplate.opsForHash().hasKey("keyword", prevCntKey)) {
            redisKeywordTemplate.opsForHash().put("keyword", prevCntKey, "1");
        }

        // 업데이트 시간 저장
        redisKeywordTemplate.opsForHash().put("keyword", updatedKey, currentTime);
    }

    private String getRedisKey(String keyword, String suffix) {
        return keyword + ":" + suffix;
    }

}
