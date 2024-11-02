package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.image.service.ImageDetailService;
import com.singlebungle.backend.domain.image.service.ImageManagementService;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {

    private final OpenaiService openaiService;
    private final GoogleVisionService googleVisionService;
    private final ImageService imageService;
    private final KeywordService keywordService;
    private final ImageDetailService imageDetailService;
    private final ImageManagementService imageManagementService;

    @PostMapping("/web")
    @Operation(summary = "웹 이미지 저장", description = "웹에서 새로운 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromWeb(
            @RequestParam("sourceUrl") String sourceUrl,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "directoryId", required = false, defaultValue = "0") String directoryIdStr
            ) {
        try {
            Long directoryId = Long.parseLong(directoryIdStr);

            // google vision api
            List<String> labels = googleVisionService.analyzeImage(imageUrl);

//            if (labels == null) {
//                throw new InvalidImageException();
//            }

            // chatgpt api
            List<String> keywords = openaiService.requestImageAnalysis(imageUrl, labels);
            if (keywords == null) {

                throw new InvalidImageException();

            } else {

                // s3 이미지 저장
//                Long imageId = imageService.getImageId(sourceUrl);
                String filename = imageService.uploadImageFromUrlToS3(imageUrl);

                // 이미지 데이터 생성, 저장
                imageService.saveImage(sourceUrl, filename, directoryId);
                // 키워드 데이터 생성, 저장
                keywordService.saveKeyword(keywords);
                // 이미지 디테일 데이터 생성, 저장
                imageDetailService.saveImageDetail(sourceUrl, filename, keywords);
            }

            /*
             라벨 데이터 저장
            */
            //


            log.info(">> [POST] /image/web");

        } catch (Exception e) {
            throw new RuntimeException(">>> imageController - 웹 이미지 저장을 실패했습니다. " + e);
        }

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp(
            @RequestParam @Valid Long imageId,
            @RequestParam @Valid Long directoryId
    ) {

        imageManagementService.saveImageManagement(imageId, directoryId);

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "앱 이미지를 저장했습니다."));
    }


}
