package com.singlebungle.backend.domain.directory.controller;

import com.singlebungle.backend.domain.directory.service.DirectoryServiceImpl;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directory")
public class DirectoryController {

    private final DirectoryServiceImpl directoryService;

    @PostMapping()
    @Operation(summary = "디렉토리 생성")
    public ResponseEntity<BaseResponseBody> create(
            @RequestBody @Valid String directoryName
    ) {

        directoryService.saveDirectory(directoryName);

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "새로운 디렉토리를 생성했습니다."));
    }

}
