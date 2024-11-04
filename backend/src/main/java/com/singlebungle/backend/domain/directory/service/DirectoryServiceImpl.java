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

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public List<Directory> createDirectory(String directoryName, String token) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

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
        Long userId = jwtProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Directory directory = directoryRepository.findByDirectoryIdAndUser(directoryId, user)
                .orElseThrow(() -> new EntityNotFoundException("Directory not found"));

        if (directory.getStatus() == 0 || directory.getStatus() == 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 디렉토리는 이름을 변경할 수 없습니다.");
        }

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

        for (int i = 0; i < directorySequence.size(); i++) {
            Long directoryId = directorySequence.get(i);
            Directory directory = directoryRepository.findByDirectoryIdAndUser(directoryId, user)
                    .orElseThrow(() -> new EntityNotFoundException("Directory not found"));

            directory.setOrder(i + 1); // 순서 설정
            directoryRepository.save(directory);
        }

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
                .order(1)
                .status(2)
                .build();

        directoryRepository.saveAll(Arrays.asList(defaultDirectory, trashDirectory));
    }
}