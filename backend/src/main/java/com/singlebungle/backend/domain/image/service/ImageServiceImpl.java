package com.singlebungle.backend.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;

    private final String bucketName = "sgbgbucket";

    @Override
    public void uploadImageFromUrlToS3(String imageUrl) {
        try {
            // URL에서 InputStream 열기
            URL url = new URL(imageUrl);
            URLConnection urlConnection = url.openConnection();

            // Content-Type으로 확장자 결정
            String contentType = urlConnection.getContentType();
            String extension = getExtensionFromContentType(contentType);

            // URL에서 파일명 생성 (고유한 UUID + 확장자)
            String fileName = UUID.randomUUID().toString() + extension;

            try (InputStream inputStream = urlConnection.getInputStream()) {
                // 메타데이터 설정
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                metadata.setContentLength(urlConnection.getContentLengthLong());

                // S3에 파일 업로드
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
                amazonS3.putObject(putObjectRequest);

            }
        } catch (Exception e) {
            throw new RuntimeException("이미지를 S3에 업로드하는 동안 오류가 발생했습니다.", e);
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
    public void saveImage(String imageUrl, String webUrl, Long directoryId) {
        boolean isImage = imageRepository.existsBySourceUrlAndImageUrl(webUrl, imageUrl);

        if (isImage)
            throw new EntityIsFoundException("이미 해당 이미지 데이터가 존재합니다");

        Image image = Image.convertToEntity(imageUrl, webUrl, directoryId);
        imageRepository.save(image);
    }

}
