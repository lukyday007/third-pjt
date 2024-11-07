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
import com.singlebungle.backend.domain.image.repository.ImageDetailRepository;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepositorySupport;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
    private final ImageDetailRepository imageDetailRepository;
    private final ImageManagementRepository imageManagementRepository;
    private final ImageManagementRepositorySupport imageManagementRepositorySupport;
    private final DirectoryRepository directoryRepository;
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

                // 파일명 생성
                String extension = getExtensionFromContentType(contentType);
                fileName = UUID.randomUUID().toString() + extension;
            } else {
                // URL 이미지 처리
                URL url = new URL(imageUrl);
                URLConnection urlConnection = url.openConnection();
                contentType = urlConnection.getContentType();
                inputStream = urlConnection.getInputStream();

                // 파일명 생성
                String extension = getExtensionFromContentType(contentType);
                fileName = UUID.randomUUID().toString() + extension;
            }

            // S3에 업로드
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(inputStream.available());

            // S3에 업로드 요청 생성
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
            amazonS3.putObject(request);

            System.out.println("===> S3에 이미지 업로드 성공: " + fileName);

            return fileName;

        } catch (MalformedURLException e) {
            log.error(">>> Invalid image URL format: {}", imageUrl, e);
            throw new RuntimeException(">>> Invalid image URL format.", e);
        } catch (IOException e) {
            log.error(">>> Failed to open URL connection for image: {}", imageUrl, e);
            throw new RuntimeException(">>> Failed to open URL connection for image.", e);
        } catch (AmazonServiceException e) {
            log.error(">>> Amazon S3 service error: {}", e.getMessage(), e);
            throw new RuntimeException(">>> Amazon S3 service error.", e);
        } catch (SdkClientException e) {
            log.error(">>> S3 client error: {}", e.getMessage(), e);
            throw new RuntimeException(">>> S3 client error.", e);
        } catch (Exception e) {
            throw new RuntimeException("-=-=-=> 이미지를 S3에 업로드하는 동안 오류가 발생했습니다.", e);
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
                return ".webp";
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
        if (isImage)
            throw new EntityIsFoundException("이미 해당 이미지 데이터가 존재합니다");

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

        return imageManagementRepositorySupport.findImageListFromDir(requestDTO);
    }


    @Override
    public Map<String, Object> getImageListFromFeed(ImageListGetRequestDTO requestDTO) {

        return imageManagementRepositorySupport.findImageListFromFeed(requestDTO);
    }


    @Override
    public ImageInfoResponseDTO getImageInfo(Long imageId) {

        Image image = imageRepository.findById(imageId).orElseThrow(() -> new EntityNotFoundException("일치하는 이미지 데이터가 존재하지 않습니다."));

        List<String> keywordToStr = imageDetailRepository.findAllByImage(image)
                .stream()
                .map(ImageDetail::getKeyword)          // ImageDetail에서 Keyword 엔티티 추출
                .map(Keyword::getKeywordName)           // Keyword 엔티티에서 keywordName 추출
                .collect(Collectors.toList());

        return ImageInfoResponseDTO.convertToDTO(image, keywordToStr);

    }


}
