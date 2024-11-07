package com.singlebungle.backend.domain.directory.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.image.entity.Image;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import com.singlebungle.backend.domain.image.repository.ImageManagementRepository;
import com.singlebungle.backend.domain.image.repository.ImageRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ImageRepository imageRepository;
    private final ImageManagementRepository imageManagementRepository;

    public List<Directory> createDirectory(String directoryName, String token) {
        // 디렉토리 이름이 null인 경우 예외 처리
        if (directoryName == null || directoryName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "디렉토리 이름은 필수입니다.");
        }

        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 디렉토리 이름 중복 체크
        if (directoryRepository.existsByNameAndUser(directoryName, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 동일한 이름의 디렉토리가 존재합니다.");
        }

        int order = directoryRepository.findMaxOrderByUser(user) + 1;

        Directory directory = Directory.builder()
                .name(directoryName)
                .order(order)
                .user(user)
                .status(1)
                .build();

        directoryRepository.save(directory);
        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }

    public List<Directory> updateDirectoryName(Long directoryId, String directoryName, String token) {
        // 디렉토리 이름이 null 또는 빈 문자열인 경우 예외 처리
        if (directoryName == null || directoryName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "디렉토리 이름은 필수입니다.");
        }

        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Directory directory = directoryRepository.findByDirectoryIdAndUser(directoryId, user)
                .orElseThrow(() -> new EntityNotFoundException("Directory not found"));

        // 디렉토리 이름 중복 체크 (수정하려는 디렉토리 제외)
        if (directoryRepository.existsByNameAndUser(directoryName, user) && !directory.getName().equals(directoryName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 동일한 이름의 디렉토리가 존재합니다.");
        }

        // 디렉토리 상태가 0 또는 2인 경우 이름 변경 불가
        if (directory.getStatus() == 0 || directory.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 디렉토리는 이름을 변경할 수 없습니다.");
        }

        // 디렉토리 이름 변경
        directory.setName(directoryName);
        directoryRepository.save(directory);

        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }

    public List<Directory> getUserDirectories(String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }

    public List<Directory> updateDirectorySequence(List<Long> directorySequence, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 상태가 1인 디렉토리만 순서를 변경하려는 디렉토리로 필터링
        List<Directory> directoriesToUpdate = directoryRepository.findAllById(directorySequence).stream()
                .filter(directory -> directory.getStatus() == 1)  // 상태가 1인 디렉토리만
                .collect(Collectors.toList());

        // Map을 사용하여 디렉토리 ID를 키로, 디렉토리 객체를 값으로 저장
        Map<Long, Directory> directoryMap = directoriesToUpdate.stream()
                .collect(Collectors.toMap(Directory::getDirectoryId, directory -> directory));

        // 상태가 1인 디렉토리만 순서 변경
        for (int i = 0; i < directorySequence.size(); i++) {
            Long directoryId = directorySequence.get(i);
            Directory directory = directoryMap.get(directoryId);
            if (directory != null) {
                directory.setOrder(i + 1);  // 순서 설정
                directoryRepository.save(directory);
            }
        }

        // 순서가 변경된 디렉토리를 포함해 유저의 모든 디렉토리 목록을 순서대로 반환
        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }


    // 디렉토리 삭제
    public List<Directory> deleteDirectory(Long directoryId, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 특정 사용자에 대한 디렉토리 조회
        Directory directory = directoryRepository.findByDirectoryIdAndUser(directoryId, user)
                .orElseThrow(() -> new EntityNotFoundException("Directory not found"));

        // 디렉토리 상태 체크
        if (directory.getStatus() == 0 || directory.getStatus() == 2) {
            throw new UnsupportedOperationException("기본 디렉토리와 휴지통 디렉토리는 삭제할 수 없습니다.");
        }

        // 디렉토리 내의 이미지들을 휴지통으로 이동 (디렉토리 삭제 전에)
        moveImagesToTrashByDirectory(directory);

        // 디렉토리 삭제
        directoryRepository.delete(directory);

        // 삭제 후 사용자 디렉토리 목록 조회
        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }

    // 디렉토리 내의 이미지들을 휴지통으로 이동
    private void moveImagesToTrashByDirectory(Directory directory) {
        // 해당 디렉토리에 속한 모든 이미지 관리 목록 조회
        List<ImageManagement> imageManagementList = imageManagementRepository.findByCurDirectory(directory);

        // 휴지통 디렉토리 조회
        Directory trashDirectory = directoryRepository.findByUserAndStatus(directory.getUser(), 2)
                .orElseThrow(() -> new EntityNotFoundException("휴지통 디렉토리가 존재하지 않습니다."));

        // 이미지들을 휴지통으로 이동
        for (ImageManagement imageManagement : imageManagementList) {
            // 이미지의 prevDirectory와 curDirectory를 수정
            Directory prevDirectory = imageManagement.getCurDirectory();

            // 현재 디렉토리가 이미 휴지통이 아니라면
            if (!imageManagement.getCurDirectory().equals(trashDirectory)) {
                // 이미지를 휴지통으로 이동
                imageManagement.setPrevDirectory(null);  // prevDirectory를 유지
                imageManagement.setCurDirectory(trashDirectory);  // 현재 디렉토리를 휴지통으로 변경
                imageManagementRepository.save(imageManagement); // 변경 사항 저장
            }
        }
    }

    // 기본 디렉토리 생성 메서드
    public void createDefaultDirectories(User user) {
        Directory defaultDirectory = Directory.builder()
                .name("기본 디렉토리")
                .user(user)
                .order(0)
                .status(0)
                .build();

        Directory trashDirectory = Directory.builder()
                .name("휴지통")
                .user(user)
                .order(0)
                .status(2)
                .build();

        directoryRepository.saveAll(Arrays.asList(defaultDirectory, trashDirectory));
    }

    @Transactional
    public void deleteImagesInBinDirectory(String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 상태가 2인 유저의 빈 디렉토리 찾기
        Directory binDirectory = directoryRepository.findByUserAndStatus(user, 2)
                .orElseThrow(() -> new IllegalStateException("빈 디렉토리가 존재하지 않습니다."));

        // 빈 디렉토리에 연결된 ImageManagement 엔티티 삭제
        imageManagementRepository.deleteByCurDirectory(binDirectory);
    }

    // 디렉토리로 이미지 이동
    @Override
    public List<ImageManagement> moveImagesToDirectory(List<Long> imageManagementIds, Long directoryId, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Directory newDirectory = (directoryId == null || directoryId == 0)
                ? directoryRepository.findByUserAndStatus(user, 0).orElseThrow(() -> new EntityNotFoundException("Default directory not found"))
                : directoryRepository.findById(directoryId).orElseThrow(() -> new EntityNotFoundException("Target directory not found"));

        List<ImageManagement> imageManagementList = imageManagementRepository.findAllById(imageManagementIds);
        for (ImageManagement imageManagement : imageManagementList) {
            Directory prevDirectory = imageManagement.getCurDirectory();
            if (!prevDirectory.equals(newDirectory)) {
                imageManagement.setPrevDirectory(prevDirectory);
                imageManagement.setCurDirectory(newDirectory);
                imageManagementRepository.save(imageManagement);
            }
        }
        return imageManagementList;
    }

    // 이미지들을 휴지통으로 이동
    @Override
    public List<ImageManagement> moveImagesToTrash(List<Long> imageManagementIds, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Directory trashDirectory = directoryRepository.findByUserAndStatus(user, 2)
                .orElseThrow(() -> new EntityNotFoundException("Trash directory not found"));

        List<ImageManagement> imageManagementList = imageManagementRepository.findAllById(imageManagementIds);

        List<ImageManagement> movedImages = new ArrayList<>();
        for (ImageManagement imageManagement : imageManagementList) {
            if (!imageManagement.getCurDirectory().equals(trashDirectory)) {
                imageManagement.setPrevDirectory(imageManagement.getCurDirectory());
                imageManagement.setCurDirectory(trashDirectory);
                imageManagementRepository.save(imageManagement);
                movedImages.add(imageManagement);
            }
        }
        return movedImages;
    }

    // 이미지들을 복원
    @Override
    public List<ImageManagement> restoreImagesFromTrash(List<Long> imageManagementIds, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Directory defaultDirectory = directoryRepository.findByUserAndStatus(user, 0)
                .orElseThrow(() -> new EntityNotFoundException("Default directory not found"));

        List<ImageManagement> imageManagementList = imageManagementRepository.findAllById(imageManagementIds);

        List<ImageManagement> restoredImages = new ArrayList<>();
        for (ImageManagement imageManagement : imageManagementList) {
            if (imageManagement.getCurDirectory().getStatus() == 2) {
                Directory prevDirectory = imageManagement.getPrevDirectory();
                imageManagement.setCurDirectory(prevDirectory != null ? prevDirectory : defaultDirectory);
                imageManagementRepository.save(imageManagement);
                restoredImages.add(imageManagement);
            }
        }
        return restoredImages;
    }

}