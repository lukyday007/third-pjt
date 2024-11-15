package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.exception.EntityIsFoundException;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageManagementServiceImpl implements ImageManagementService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final ImageManagementRepository imageManagementRepository;
    private final DirectoryRepository directoryRepository;

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
        imageRepository.save(image);
    }

    @Override
    @Transactional
    public void deleteImages(ImageIdDeleteRequestDTO requestDTO) {

        for (Long imageManagementId : requestDTO.getImageManagementIds()) {
            if (imageManagementRepository.existsById(imageManagementId)) {
                imageManagementRepository.deleteById(imageManagementId);
            }
        }
    }
}