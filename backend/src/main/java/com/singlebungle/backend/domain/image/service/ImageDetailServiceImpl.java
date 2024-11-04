package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageDetail;
import com.singlebungle.backend.domain.image.repository.ImageDetailRepository;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.keyword.entity.Keyword;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDetailServiceImpl implements ImageDetailService {
    private final ImageRepository imageRepository;
    private final KeywordRepository keywordRepository;
    private final ImageDetailRepository imageDetailRepository;

    @Override
    @Transactional
    public void saveImageDetail(String sourceUrl, String imageUrl, List<String> keywords) {
        // 이미지 조회
        Image image = imageRepository.findBySourceUrlAndImageUrl(sourceUrl, imageUrl)
                .orElseThrow(() -> new EntityNotFoundException("해당 이미지 데이터가 존재하지 않습니다."));


        // 키워드 조회
        for (String name : keywords) {
            boolean isKeyword = keywordRepository.existsByKeywordName(name);

            if (isKeyword) {
                Keyword kw = keywordRepository.findByKeywordName(name);

                // ImageDetail 생성, 저장
                ImageDetail imageDetail = ImageDetail.convertToEntity(image, kw);
                imageDetailRepository.save(imageDetail);
            }
        }
    }

    @Override
    public void deleteImages(ImageIdDeleteRequestDTO requestDTO) {
        List<Long> imageDetailIds = requestDTO.getImageDetailIds();
        // imageDetail에 있는 해당 이미지 ID들을 삭제
        try {
            imageDetailRepository.deleteByImageDetailIdIn(imageDetailIds);
            log.info(">>> imageDetailIds {}에 대한 ImageDetail 항목이 성공적으로 삭제되었습니다.", Arrays.toString(imageDetailIds.toArray()));
        } catch (Exception e) {
            log.error(">>> imageDetailIds {} 삭제 중 오류가 발생했습니다: {}",  Arrays.toString(imageDetailIds.toArray()), e.getMessage());
            throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.", e);
        }
    }
}
