package com.singlebungle.backend.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.singlebungle.backend.domain.image.entity.Image;

import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.ImageSaveException;
import com.singlebungle.backend.global.exception.InvalidRequestException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;


    @Override
    public void uploadImageFromUrlToS3(String url) {
        try {
            // url 디코딩
            String decodeUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            log.info("Decoded URL: {}", decodeUrl);

            // S3 버킷과 키 추출
            String bucketName = "sgbgbucket";
            String key;
            String s3Prefix = "https://sgbgbucket.s3.ap-northeast-2.amazonaws.com/";
            int startIdx = decodeUrl.indexOf(s3Prefix) + s3Prefix.length();
            int endIdx = decodeUrl.indexOf("?");
            key = url.substring(startIdx, endIdx);
            log.info(">>> saveImage 파라미터 - Bucket: {}, Key: {}", bucketName, key);

        } catch (UnsupportedEncodingException | AmazonS3Exception e) {
            log.error(">>> Error occurred while saving image to S3", e);
            throw new ImageSaveException("이미지 저장 중 오류가 발생했습니다: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            log.error(">>> Invalid request: URL 형식 오류", e);
            throw new InvalidRequestException("잘못된 요청입니다. URL 형식이 올바르지 않습니다. : " + e.getMessage());
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
