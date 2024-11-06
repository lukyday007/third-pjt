package com.singlebungle.backend.domain.directory.service;

import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.repository.DirectoryRepository;
import com.singlebungle.backend.domain.user.entity.User;
import com.singlebungle.backend.domain.user.repository.UserRepository;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

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
                .toList();

        // 상태가 1인 디렉토리만 순서 변경
        for (int i = 0; i < directoriesToUpdate.size(); i++) {
            Directory directory = directoriesToUpdate.get(i);
            directory.setOrder(i + 1);  // 순서 설정
            directoryRepository.save(directory);
        }

        // 순서가 변경된 디렉토리를 포함해 유저의 모든 디렉토리 목록을 순서대로 반환
        return directoryRepository.findAllByUserOrderByOrderAsc(user);
    }

    public List<Directory> deleteDirectory(Long directoryId, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 특정 사용자에 대한 디렉토리 조회
        Directory directory = directoryRepository.findByDirectoryIdAndUser(directoryId, user)
                .orElseThrow(() -> new EntityNotFoundException("Directory not found"));

        // 디렉토리 상태 체크
        if (directory.getStatus() == 0 || directory.getStatus() == 2) {
            throw new UnsupportedOperationException("기본 디렉토리와 휴지통 디렉토리는 삭제할 수 없습니다.");
        }

        // 디렉토리 삭제
        directoryRepository.delete(directory);

        // 삭제 후 사용자 디렉토리 목록 조회
        return directoryRepository.findAllByUserOrderByOrderAsc(user);
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
}