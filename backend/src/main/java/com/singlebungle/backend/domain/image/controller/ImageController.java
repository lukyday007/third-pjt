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

            // chatgpt api
            if (labels == null) {
                throw new InvalidImageException();
            } else {
                String labelsToString = String.join(", ", labels);
                // 요청 프롬프트
                String requestText = String.format(
                        "### 라벨 번역\n"
                                + "우선 labels 배열에 있는 목록을 명사로 번역해서 리스트로 돌려줘 [%s].\n"
                                + "이때 들여쓰기하고 번역한 결과를 써야해.\n\n"
                                + "### 이미지에 있는 텍스트\n"
                                + "이미지에 텍스트가 있으면 텍스트를 학습하고 반환해. 없으면 '텍스트 없음'이라고 작성해.\n\n"
                                + "### 키워드\n"
                                + "이 이미지에 대한 키워드를 5개 정도 추출해서 번호와 함께 리스트로 반환해줘. 이때 '텍스트' 나 '언어', '유머', '인물'은 없는 키워드여야 해",
                        labelsToString
                );

                openaiService.requestImageAnalysis(imageUrl, requestText);
            }
        } catch (Exception e) {
            throw new RuntimeException(">>> 이미지 분석을 실패했습니다. " + e);
        }

        /*
         s3 이미지 저장
        */
        imageService.saveImage(imageUrl);
        log.info(">> [POST] /image/web");

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp() {

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "앱 이미지를 저장했습니다."));
    }


}
