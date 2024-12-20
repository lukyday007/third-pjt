package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
import com.singlebungle.backend.domain.ai.dto.response.KeywordsFromOpenAi;
import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.image.dto.request.ImageAppRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageWebRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import com.singlebungle.backend.domain.image.service.ImageDetailService;
import com.singlebungle.backend.domain.image.service.ImageManagementService;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.exception.InvalidResponseException;
import com.singlebungle.backend.global.exception.UrlAccessException;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final UserService userService;
    private final OpenaiService openaiService;
    private final GoogleVisionService googleVisionService;
    private final ImageService imageService;
    private final KeywordService keywordService;
    private final SearchService searchService;
    private final ImageDetailService imageDetailService;
    private final ImageManagementService imageManagementService;


    @PostMapping("/web")
    @Operation(summary = "웹 이미지 저장", description = "웹에서 새로운 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromWeb(
            @RequestBody @Valid ImageWebRequestDTO requestDTO,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization") String token
    ) {

        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        long totalStartTime = System.currentTimeMillis();

        try {
            Long directoryId = requestDTO.getDirectoryId();

            // google vision api -> 라벨 번역 안됨
            long startTime = System.currentTimeMillis();
            log.info(">>> Google Vision 병렬 호출 시작");
            List<String> labels = googleVisionService.analyzeImage(requestDTO.getImageUrl());
            log.info(">>> Google Vision API 호출 완료, 반환 라벨: {}", labels);
            log.info(">>> Google Vision 처리 시간: {} ms", System.currentTimeMillis() - startTime);


            // chatgpt api
            startTime = System.currentTimeMillis();
            log.info(">>> ChatGPT API 호출 시작");
            KeywordAndLabels keywordAndLabels = openaiService.requestImageAnalysis(requestDTO.getImageUrl(), labels);
            log.info(">>> ChatGPT API 호출 완료, 반환 키워드: {}, 태그: {}", keywordAndLabels.getKeywords(), keywordAndLabels.getLabels());
            log.info(">>> OpenAI 처리 시간: {} ms", System.currentTimeMillis() - startTime);

            List<String> result = null;

            // OpenAI에서 키워드가 반환된 경우
            if (keywordAndLabels.getKeywords() != null && !keywordAndLabels.getKeywords().isEmpty()) {
                result = keywordAndLabels.getKeywords();

            // OpenAI에서 키워드가 반환되지 않은 경우 Google Vision의 라벨로 대체
            } else if (keywordAndLabels.getLabels() != null && !keywordAndLabels.getLabels().isEmpty()) {
                result = keywordAndLabels.getLabels();

            // 둘 다 비어 있거나 null인 경우 예외 처리
            } else {
                throw new InvalidResponseException(">>>> 키워드를 저장할 수 없는 이미지입니다.");
            }

            // s3 이미지 저장
            String filename = imageService.uploadImageFromUrlToS3(requestDTO.getImageUrl());
            // 이미지 데이터 생성, 저장
            imageService.saveImage(userId, requestDTO.getSourceUrl(), filename, directoryId);
            // 키워드 데이터 생성, 저장
            keywordService.saveKeyword(result);
            // 이미지 디테일 데이터 생성, 저장
            imageDetailService.saveImageDetail(requestDTO.getSourceUrl(), filename, result);
            // 테그 생성, 저장
            searchService.saveTagsByKeywords(result, filename);

            long totalEndTime = System.currentTimeMillis();
            log.info(">>> [POST] /images/web - 전체 처리 시간: {} ms", totalEndTime - totalStartTime);

        } catch (UrlAccessException e) {
            log.error(">>> URL 접근 불가: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error(">>> 이미지 저장 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("웹 이미지 저장 중 오류가 발생했습니다.", e);
        }

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp(
            @RequestBody @Valid ImageAppRequestDTO requestDTO,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization") String token
            ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [POST] /images/app - 요청 dto : {}, userId : {}", requestDTO.toString(), userId);

        imageManagementService.saveImageManagement(userId, requestDTO.getImageId(), requestDTO.getDirectoryId());

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "앱 이미지를 저장했습니다."));
    }


    // my?directoryId=”long”&page=”int”&size=”int”&keyword=”string”&sort=”int”
    // sort : 0 - 최신 / 1 - 오래된 / 2 - 랜덤
    @GetMapping("/my")
    @Operation(summary = "디렉토리 안 이미지 조회", description = "디렉토리 내의 이미지를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getImageListFromDirectory(
            @Parameter(description = "디렉토리 번호")
            @RequestParam(value = "directoryId", required = false, defaultValue = "0") Long directoryId,
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "이미지 개수")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "키워드")
            @RequestParam(value = "keyword", required = false) String keywordList,
            @Parameter(description = "정렬기준 (0: 최신, 1: 오래 된, 2: 랜덤")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort,
            @Parameter(description = "휴지통 여부 확인")
            @RequestParam(value = "bin", required = false, defaultValue = "false") Boolean isBin,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization") String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [GET] /images/my - 요청 파라미터: userId - {}, directoryId - {}, page - {}, size - {}, keyword - {}, sort - {}", userId, directoryId, page, size, keywordList, sort);

        Map<String, Object> imageList;

        if (keywordList != null && !keywordList.isEmpty()) {
            List<String> keywords = Arrays.asList(keywordList.split(","));
            ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(userId, directoryId, page, size, keywords, sort, isBin);
            imageList = imageService.getImageListFromDir(requestDTO);
        } else {
            ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(userId, directoryId, page, size, sort, isBin);
            imageList = imageService.getImageListFromDir(requestDTO);
        }

        return ResponseEntity.status(200).body(imageList);
    }


    // feed?page=”int”&size=”int”&keyword=”string”&sort=”int”
    // sort : 0 - 최신 / 1 - 오래된 / 2 - 랜덤
    @GetMapping("/feed")
    @Operation(summary = "홈 피드 이미지 조회", description = "홈 피드의 이미지를 조회합니다.")    public ResponseEntity<Map<String, Object>> getImageListFromFeed(
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "이미지 개수")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "키워드")
            @RequestParam(value = "keyword", required = false) String keywordList,
            @Parameter(description = "정렬기준 (0: 최신, 1: 오래 된, 2: 랜덤")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort
    ) {
        log.info(">>> [GET] /images/feed - 요청 파라미터:  page - {}, size - {}, keyword - {}, sort - {}" , page, size, keywordList, sort);

        Map<String, Object> imageList;

        if (keywordList != null && !keywordList.isEmpty()) {
            List<String> keywords = Arrays.asList(keywordList.split(","));
            ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(page, size, keywords, sort);
            imageList = imageService.getImageListFromFeed(requestDTO);
        } else {
            ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(page, size, sort);
            imageList = imageService.getImageListFromFeed(requestDTO);
        }

        return ResponseEntity.status(200).body(imageList);
    }


    // /images/{imageId}
    @GetMapping(value = "/{imageId}")
    @Operation(summary = "이미지 상세 조회", description = "해당 이미지를 상세 조회합니다.")
    public ResponseEntity<ImageInfoResponseDTO> getImageDetail(
        @PathVariable Long imageId
    ) {
        log.info(">>> [GET] /images/{} - 요청 파라미터: imageId - {}", imageId, imageId);
        ImageInfoResponseDTO imageInfo = imageService.getImageInfo(imageId);

        return ResponseEntity.status(200).body(imageInfo);
    }


    // imageId: “List<Long>”
    @DeleteMapping()
    @Operation(summary = "이미지 삭제", description = "해당 이미지를 삭제합니다..")
    public ResponseEntity<BaseResponseBody> deleteImages(
            @RequestBody ImageIdDeleteRequestDTO requestDTO,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        log.info(">>> [DELETE] /images 삭제요청하는 이미지Id - {}", Arrays.toString(requestDTO.getImageManagementIds().toArray()));

        imageManagementService.deleteImages(requestDTO);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "이미지들을 영구 삭제합니다."));
    }

}