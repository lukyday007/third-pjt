package com.singlebungle.backend.domain.image.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Directory directory;

        if (directoryId != 0L) {
            directory = directoryRepository.findById(directoryId).orElseThrow(() -> new EntityNotFoundException("해당 디렉터리 데이터가 존재하지 않습니다. :" + directoryId));
        } else {
            int status = 0;
            directory = directoryRepository.findByUserAndStatus(user, status)
                    .orElseThrow(() -> new IllegalStateException("디렉토리가 존재하지 않습니다."));;
        }

        ImageManagement imageManagement = ImageManagement.convertToEntity(user, image, directory);
        imageManagementRepository.save(imageManagement);
    }
}