package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
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

    @PostMapping("/web")
    @Operation(summary = "웹 이미지 저장", description = "웹에서 새로운 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromWeb(
            @RequestParam("webUrl") String webUrl,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "directoryId", required = false, defaultValue = "0") Long directoryId
    ) {
        // google vision
        try {
            List<String> labels = googleVisionService.analyzeImage(imageUrl);
            if (labels != null) {

                String labelsToString = String.join(", ", labels);

                String requestText = String.format(
                        "우선 labels 배열에 있는 목록을 명사로 번역해서 리스트로 돌려줘 [%s]. 이때 ### 라벨 들여쓰기 하고 번역한 결과를 써야해. 다음은 이미지에 텍스트가 있는 경우와 없는 경우로 나뉘어. 이미지에 텍스트가 있으면 텍스트를 반환하고 너는 학습해. 그 다음에 이 이미지에 대한 키워드를 5개 정도 추출해서 리스트로 반환해줘.",
                        labelsToString
                );
                openaiService.requestImageAnalysis(imageUrl, requestText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // chatgpt
//        String requestText = "우선 labels배열에 있는 목록을 명사로 번역해서 리스트로 돌려줘 ${labels} 이미지에 텍스트가 있는 경우와 없는 경우로 나뉘어. 이미지에 텍스트가 있으면 텍스트를 반환하고 너는 학습해. 그 다음에 이 이미지에 대한 키워드를 5개 정도 추출해서 리스트로 반환해줘.";

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp() {

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "앱 이미지를 저장했습니다."));
    }


}
