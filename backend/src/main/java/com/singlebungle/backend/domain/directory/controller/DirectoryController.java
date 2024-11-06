package com.singlebungle.backend.domain.directory.controller;

import com.singlebungle.backend.domain.directory.dto.request.DirectoryRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectorySequenceRequestDTO;
import com.singlebungle.backend.domain.directory.dto.request.DirectoryUpdateRequestDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryListResponseDTO;
import com.singlebungle.backend.domain.directory.dto.response.DirectoryResponseDTO;
import com.singlebungle.backend.domain.directory.entity.Directory;
import com.singlebungle.backend.domain.directory.service.DirectoryService;
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
        List<DirectoryResponseDTO> response = directories.stream()
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(new DirectoryListResponseDTO(response));
    }

    // 디렉토리 이름 수정
    @PatchMapping
    public ResponseEntity<?> updateDirectoryName(
            @RequestBody DirectoryUpdateRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectoryName(request.getDirectoryId(), request.getDirectoryName(), token);
        List<DirectoryResponseDTO> response = directories.stream()
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new DirectoryListResponseDTO(response));
    }

    // 디렉토리 목록 조회
    @GetMapping
    public ResponseEntity<?> getUserDirectories(@RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.getUserDirectories(token);
        List<DirectoryResponseDTO> response = directories.stream()
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new DirectoryListResponseDTO(response));
    }

    // 디렉토리 순서 변경
    @PatchMapping("/sequence")
    public ResponseEntity<?> updateDirectorySequence(
            @RequestBody DirectorySequenceRequestDTO request,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.updateDirectorySequence(request.getDirectorySequence(), token);
        List<DirectoryResponseDTO> response = directories.stream()
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new DirectoryListResponseDTO(response));
    }

    // 디렉토리 삭제
    @DeleteMapping("/{directoryId}")
    public ResponseEntity<?> deleteDirectory(
            @PathVariable Long directoryId,
            @RequestHeader("Authorization") String token) {
        List<Directory> directories = directoryService.deleteDirectory(directoryId, token);
        List<DirectoryResponseDTO> response = directories.stream()
                .map(directory -> new DirectoryResponseDTO(directory.getDirectoryId(), directory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new DirectoryListResponseDTO(response));
    }
}