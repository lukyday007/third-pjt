package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {
    private final OpenaiService openaiService;
    private final GoogleVisionService googleVisionService;
    private final ImageService imageService;

    @PostMapping("/web")
    @Operation(summary = "웹 이미지 저장", description = "웹에서 새로운 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromWeb(
            @RequestBody @Valid ImageWebRequestDTO dto
            ) {
        try {
            // google vision api
            List<String> labels = googleVisionService.analyzeImage(dto.getImageUrl());
            if (labels == null) {
                throw new InvalidImageException();
            }

            // chatgpt api
            String resultContent = openaiService.requestImageAnalysis(dto.getImageUrl(), labels);
            if (resultContent == null) {
                throw new InvalidImageException();
            }

            /*
             s3 이미지 저장
            */
    //        imageService.uploadImageFromUrlToS3(imageUrl);
    //        log.info(">> [POST] /image/web");

            // 이미지 데이터 생성, 저장
            imageService.saveImage(dto);


        } catch (Exception e) {
            throw new RuntimeException(">>> 웹 이미지 저장을 실패했습니다. " + e);
        }


        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp() {

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "앱 이미지를 저장했습니다."));
    }


}
