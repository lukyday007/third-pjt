package com.singlebungle.backend.domain.directory.controller;

import com.singlebungle.backend.domain.directory.dto.request.DirectoryRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectorySequenceRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectoryUpdateRequestDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryListResponseDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryResponseDTO;
import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.service.DirectoryService;
import com.singlebungle.backend.domain.image.dto.request.ImageManagementIdsRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.MoveImagesRequestDTO;
import com.singlebungle.backend.domain.image.entity.ImageManagement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "디렉토리 생성", description = "새 디렉토리를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "디렉토리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @PostMapping
    public ResponseEntity<?> createDirectory(
            @RequestBody DirectoryRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.createDirectory(request.getDirectoryName(), token);
        return buildDirectoryResponse(directories, HttpStatus.CREATED);
    }

    // 디렉토리 이름 수정
    @Operation(summary = "디렉토리 이름 수정", description = "기존 디렉토리의 이름을 수정합니다.")
    @PatchMapping
    public ResponseEntity<?> updateDirectoryName(
            @RequestBody DirectoryUpdateRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectoryName(request.getDirectoryId(), request.getDirectoryName(), token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 목록 조회
    @Operation(summary = "디렉토리 목록 조회", description = "사용자의 모든 디렉토리를 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getUserDirectories(@RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.getUserDirectories(token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 순서 변경
    @Operation(summary = "디렉토리 순서 변경", description = "디렉토리의 순서를 변경합니다.")
    @PatchMapping("/sequence")
    public ResponseEntity<?> updateDirectorySequence(
            @RequestBody DirectorySequenceRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectorySequence(request.getDirectorySequence(), token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 디렉토리 삭제
    @Operation(summary = "디렉토리 삭제", description = "특정 디렉토리를 삭제합니다.")
    @DeleteMapping("/{directoryId}")
    public ResponseEntity<?> deleteDirectory(
            @PathVariable Long directoryId,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.deleteDirectory(directoryId, token);
        return buildDirectoryResponse(directories, HttpStatus.OK);
    }

    // 휴지통 비우기
    @DeleteMapping("/bin")
    @Operation(summary = "휴지통 비우기", description = "휴지통 내 모든 이미지를 삭제합니다.")
    public ResponseEntity<?> deleteImagesInBinDirectory(@RequestHeader("Authorization") String token) {
        directoryService.deleteImagesInBinDirectory(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 디렉토리로 이미지 이동
    @PatchMapping("/location")
    @Operation(summary = "디렉토리로 이미지 이동", description = "목록으로 전달받은 이미지들을 특정 디렉토리로 이동합니다.")
    public ResponseEntity<?> moveImagesToDirectory(
            @RequestBody MoveImagesRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<ImageManagement> result = directoryService.moveImagesToDirectory(request.getImageManagementIds(), request.getDirectoryId(), token);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PatchMapping("/bin")
    @Operation(summary = "휴지통 이동/복원", description = "목록으로 전달받은 이미지들을 휴지통으로 보내거나 복원합니다.")
    public ResponseEntity<?> handleImages(
            @RequestParam(defaultValue = "true") Boolean toTrash,
            @RequestBody ImageManagementIdsRequestDTO request,
            @RequestHeader("Authorization") String token) {

        List<Long> imageManagementIds = request.getImageManagementIds();
        List<ImageManagement> images;

        if (toTrash) {
            images = directoryService.moveImagesToTrash(imageManagementIds, token);
        } else {
            images = directoryService.restoreImagesFromTrash(imageManagementIds, token);
        }
        return ResponseEntity.status(HttpStatus.OK).body(images);
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
