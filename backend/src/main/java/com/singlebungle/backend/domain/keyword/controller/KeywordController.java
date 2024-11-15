package com.singlebungle.backend.domain.keyword.controller;

import com.singlebungle.backend.domain.keyword.dto.KeywordRankResponseDTO;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.domain.user.service.UserService;
import com.singlebungle.backend.global.auth.auth.JwtProvider;
import com.singlebungle.backend.global.exception.model.NoTokenRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keywords")
public class KeywordController {

    private final UserService userService;
    private final KeywordService keywordService;
    private final SearchService searchService;
    private final JwtProvider jwtProvider;


    @GetMapping()
    @Operation(summary = "키워드 검색", description = "키워드를 검색합니다.")
    public ResponseEntity<List<String>> search(
            @RequestParam String keyword,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        List<String> keywords = searchService.getKeywordsByTag(keyword);

        return ResponseEntity.status(200).body(keywords);
    }


    @GetMapping("/ranking")
    @Operation(summary = "키워드 랭킹", description = "키워드 랭킹을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getKeywordRankList() {

        Map<String, Object> keywordRank = new HashMap<>();
        List<KeywordRankResponseDTO> KeywordRankList = keywordService.getKeywordRankList();
        keywordRank.put("keywords", KeywordRankList);

        return ResponseEntity.status(200).body(keywordRank);
    }

    @GetMapping("/my")
    public ResponseEntity<List<String>> getKeywords(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam Long directoryId,
            @RequestParam(defaultValue = "false") boolean bin) {

        Long userId = extractUserIdFromToken(token);
        List<String> keywords = keywordService.getKeywords(userId, keyword, directoryId, bin);
        return ResponseEntity.ok(keywords);
    }

    private Long extractUserIdFromToken(String token) {
        // 토큰에서 "Bearer "를 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 토큰에서 userId 추출 로직 (예: jwtProvider 사용)
        return jwtProvider.getUserIdFromToken(token);
    }
}
