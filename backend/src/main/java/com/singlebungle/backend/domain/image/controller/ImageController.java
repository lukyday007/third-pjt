package com.singlebungle.backend.domain.image.controller;

import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
import com.singlebungle.backend.domain.ai.service.GoogleVisionService;
import com.singlebungle.backend.domain.ai.service.OpenaiService;
import com.singlebungle.backend.domain.image.dto.request.ImageAppRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageIdDeleteRequestDTO;
import com.singlebungle.backend.domain.image.dto.request.ImageListGetRequestDTO;
import com.singlebungle.backend.domain.image.dto.response.ImageInfoResponseDTO;
import com.singlebungle.backend.domain.image.service.ImageDetailService;
import com.singlebungle.backend.domain.image.service.ImageManagementService;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.global.exception.InvalidImageException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

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
            @RequestParam("sourceUrl") String sourceUrl,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "directoryId", required = false, defaultValue = "0") String directoryIdStr
            ) {
        try {
            Long directoryId = Long.parseLong(directoryIdStr);

            // google vision api -> 라벨 번역 안됨
            List<String> labels = googleVisionService.analyzeImage(imageUrl);

//            if (labels == null) {
//                throw new InvalidImageException();
//            }

            // chatgpt api
            KeywordAndLabels keywordAndLabels = openaiService.requestImageAnalysis(imageUrl, labels);

            if (keywordAndLabels.getKeywords() == null) {

                throw new InvalidImageException();

            } else {

                // s3 이미지 저장
                String filename = imageService.uploadImageFromUrlToS3(imageUrl);

                // 이미지 데이터 생성, 저장
                imageService.saveImage(sourceUrl, filename, directoryId);
                // 키워드 데이터 생성, 저장
                keywordService.saveKeyword(keywordAndLabels.getKeywords());
                // 이미지 디테일 데이터 생성, 저장
                imageDetailService.saveImageDetail(sourceUrl, filename, keywordAndLabels.getKeywords());
                // 테그 생성, 저장
                searchService.saveTags(keywordAndLabels.getTags(), filename);

            }

            /*
             라벨 데이터 저장
            */
            //
//            log.info(">>> [POST] /images/web - 요청 dto : {}", requestDTO.toString());

        } catch (Exception e) {
            throw new RuntimeException(">>> imageController - 웹 이미지 저장을 실패했습니다. " + e);
        }

        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "웹 이미지를 저장했습니다."));
    }


    @PostMapping("/app")
    @Operation(summary = "앱 이미지 저장", description = "앱에서 이미지를 등록합니다.")
    public ResponseEntity<BaseResponseBody> saveFromApp(
            @RequestBody ImageAppRequestDTO requestDTO
            ) {
        log.info(">>> [POST] /images/app - 요청 dto : {}", requestDTO.toString());

        imageManagementService.saveImageManagement(requestDTO.getImageId(), requestDTO.getDirectoryId());

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
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "정렬기준 (0: 최신, 1: 오래 된, 2: 랜덤")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort
//            @Parameter(description = "JWT", required = false)
//            @RequestHeader(value = "Authorization", required = false) String token

    ) {
        log.info(">>> [GET] /images/my - 요청 파라미터: directoryId - {}, page - {}, size - {}, keyword - {}, sort - {}", directoryId, page, size, keyword, sort);

        ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(directoryId, page, size, keyword, sort);
        Map<String, Object> imageList = imageService.getImageList(requestDTO);

        return ResponseEntity.status(200).body(imageList);
    }


    // feed?page=”int”&size=”int”&keyword=”string”&sort=”int”
    // sort : 0 - 최신 / 1 - 오래된 / 2 - 랜덤
    @GetMapping("/feed")
    @Operation(summary = "디렉토리 안 이미지 조회", description = "디렉토리 내의 이미지를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getImageListFromFeed(
            @Parameter(description = "페이지 번호")
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @Parameter(description = "이미지 개수")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @Parameter(description = "키워드")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "정렬기준 (0: 최신, 1: 오래 된, 2: 랜덤")
            @RequestParam(value = "sort", required = false, defaultValue = "0") int sort
//            @Parameter(description = "JWT", required = false)
//            @RequestHeader(value = "Authorization", required = false) String token

    ) {
        log.info(">>> [GET] /images/feed - 요청 파라미터:  page - {}, size - {}, keyword - {}, sort - {}" , page, size, keyword, sort);

        ImageListGetRequestDTO requestDTO = new ImageListGetRequestDTO(page, size, keyword, sort);
        Map<String, Object> imageList = imageService.getImageList(requestDTO);

        return ResponseEntity.status(200).body(imageList);
    }


    // /images/{imageId}
    //
    @GetMapping(value = "/{imageId}")
    @Operation(summary = "이미지 상세 조회", description = "해당 이미지를 상세 조회합니다.")
    public ResponseEntity<ImageInfoResponseDTO> getImage(
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
            @RequestBody ImageIdDeleteRequestDTO requestDTO
            ) {

        log.info(">>> [DELETE] /images 삭제요청하는 이미지Id - {}", Arrays.toString(requestDTO.getImageDetailIds().toArray()));

        imageDetailService.deleteImages(requestDTO);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "이미지들을 영구 삭제합니다."));
    }

}
