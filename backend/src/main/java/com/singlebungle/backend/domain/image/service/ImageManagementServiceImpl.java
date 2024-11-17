package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.repository.ImageDetailRepository;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.keyword.repository.KeywordRepository;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageManagementServiceImpl implements ImageManagementService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageDetailRepository imageDetailRepository;
    private final ImageManagementRepository imageManagementRepository;
    private final DirectoryRepository directoryRepository;
    private final KeywordService keywordService;

    @Override
    @Transactional
    public void saveImageManagement(Long userId, Long imageId, Long directoryId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당 유저 데이터가 존재하지 않습니다. :" + userId));
        Image image = imageRepository.findById(imageId).orElseThrow(() -> new EntityNotFoundException("해당 이미지 데이터가 존재하지 않습니다. :" + imageId));

        // directory => 디폴트 일 때 처리
        Directory directory;
        if (directoryId == 0) {
            int status = 0;
            directory =  directoryRepository.findByUserAndStatus(user, status)
                    .orElseThrow(() -> new IllegalStateException("디렉토리가 존재하지 않습니다."));;
        } else {
            directory = directoryRepository.findById(directoryId).orElseThrow(() -> new EntityNotFoundException("해당 디렉토리 데이터가 존재하지 않습니다. " + directoryId));
        }

//        // 이미지 중복 저장 방지
//        ImageManagement imageManagement = imageManagementRepository.findByImageAndUser(image, user);
//        if (imageManagement != null) {
//            throw new EntityIsFoundException("이미 저장한 이미지 입니다.");
//        }

        ImageManagement im = ImageManagement.convertToEntity(user, image, directory);
        imageManagementRepository.save(im);

        image.setCount(image.getCount() + 1);

        /*
        TODO 키워드 증가 로직 반영
        */
        List<String> keywords = imageDetailRepository.findAllByImage(image)
                .stream() // List<ImageDetail>에서 스트림 생성
                .map(imageDetail -> imageDetail.getKeyword().getKeywordName()) // Keyword의 이름 추출
                .distinct() // 중복 키워드 제거
                .collect(Collectors.toList()); // 리스트로 변환

        if (keywords != null) {
            for (String keyword: keywords) {
                keywordService.increaseCurCnt(keyword);
            }
        }

        imageRepository.save(image);
    }


    @Override
    @Transactional
    public void deleteImages(ImageIdDeleteRequestDTO requestDTO) {

        for (Long imageManagementId : requestDTO.getImageManagementIds()) {
            ImageManagement imageManagement = imageManagementRepository.findById(imageManagementId).orElseThrow(() -> new EntityNotFoundException("해당하는 이미지 관리 데이터가 없습니다." + imageManagementId));
            if (imageManagement != null) {
                Image image = imageRepository.findById(imageManagement.getImage().getImageId()).orElseThrow(()-> new EntityNotFoundException("해당하는 이미지 데이터가 없습니다."));
                image.setCount(image.getCount() - 1);

                if (image.getCount() <= 0 && !image.isDeleted()) {
                    image.setDeleted(true);
                }
                imageRepository.save(image);

                imageManagementRepository.deleteById(imageManagementId);
            }
        }
    }
}