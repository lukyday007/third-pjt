package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam("webUrl") String webUrl,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "directoryId", required = false, defaultValue = "0") Long directoryId
    ) {
        try {
            // google vision api
            List<String> labels = googleVisionService.analyzeImage(imageUrl);
            if (labels == null) {
                throw new InvalidImageException();
            }

            // chatgpt api
            String resultContent = openaiService.requestImageAnalysis(imageUrl, labels);
            if (resultContent == null) {
                throw new InvalidImageException();
            }

            /*
             s3 이미지 저장
            */
    //        imageService.saveImage(imageUrl);
    //        log.info(">> [POST] /image/web");

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
