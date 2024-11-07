package com.singlebungle.backend.domain.directory.controller;

import com.singlebungle.backend.domain.directory.dto.request.DirectoryRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectorySequenceRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectoryUpdateRequestDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryListResponseDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryResponseDTO;
import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.service.DirectoryService;
import com.singlebungle.backend.domain.image.dto.request.MoveImagesRequestDTO;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/directories")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    // 디렉토리 생성
    @PostMapping
    public ResponseEntity<?> createDirectory(
            @RequestBody DirectoryRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.createDirectory(request.getDirectoryName(), token);
        return buildDirectoryResponse(directories, HttpStatus.CREATED);
    }

    // 디렉토리 이름 수정
    @PatchMapping
    public ResponseEntity<?> updateDirectoryName(
            @RequestBody DirectoryUpdateRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectoryName(request.getDirectoryId(), request.getDirectoryName(), token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 목록 조회
    @GetMapping
    public ResponseEntity<?> getUserDirectories(@RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.getUserDirectories(token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 순서 변경
    @PatchMapping("/sequence")
    public ResponseEntity<?> updateDirectorySequence(
            @RequestBody DirectorySequenceRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectorySequence(request.getDirectorySequence(), token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 삭제
    @DeleteMapping("/{directoryId}")
    public ResponseEntity<?> deleteDirectory(
            @PathVariable Long directoryId,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.deleteDirectory(directoryId, token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 휴지통 비우기
    @DeleteMapping("/bin")
    public ResponseEntity<?> deleteImagesInBinDirectory(@RequestHeader("Authorization") String token) {
        directoryService.deleteImagesInBinDirectory(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/location")
    public ResponseEntity<?> moveImagesToDirectory(
            @RequestBody MoveImagesRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<ImageManagement> result = directoryService.moveImagesToDirectory(request.getImageIds(), request.getDirectoryId(), token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 이미지 휴지통으로 이동 또는 복원
    @PatchMapping
    public ResponseEntity<?> handleImages(
            @RequestParam(defaultValue = "true") Boolean toTrash,  // 기본값은 true
            @RequestBody List<Long> imageIds,  // 요청 Body로 이미지 아이디 목록 받기
            @RequestHeader("Authorization") String token) {

        if (toTrash) {
            // 이미지를 휴지통으로 이동
            List<ImageManagement> movedImages = directoryService.moveImagesToTrash(imageIds, token);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            // 이미지를 복원
            List<ImageManagement> restoredImages = directoryService.restoreImagesFromTrash(imageIds, token);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
    }

    // 중복되는 디렉토리 필터링 및 변환 처리 로직을 하나의 메서드로 추출
    private ResponseEntity<?> buildDirectoryResponse(List<Directory> directories, HttpStatus status) {
        List<DirectoryResponseDTO> response = directories.stream()
                .filter(directory -> directory.getStatus() == 1)  // 상태가 1인 디렉토리만 필터링
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.status(status).body(new DirectoryListResponseDTO(response));
    }
}
