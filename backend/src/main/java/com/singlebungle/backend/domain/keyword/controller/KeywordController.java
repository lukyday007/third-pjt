package com.singlebungle.backend.domain.keyword.controller;

import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.domain.user.service.UserService;
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
@RequestMapping("/keyword")
public class KeywordController {

    private final UserService userService;
    private final SearchService searchService;

    @GetMapping("/{keyword}")
    @Operation(summary = "키워드 검색", description = "키워드를 검색합니다.")
    public ResponseEntity<Map<String, List<String>>> search(
            @PathVariable String keyword,
            @Parameter(description = "JWT")
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        Long userId = 0L;
        if (token != null) {
            userId = userService.getUserByToken(token);
        } else {
            throw new NoTokenRequestException("유효한 유저 토큰이 없습니다.");
        }

        List<String> images = searchService.getImageUrlsByTag(keyword);

        Map<String, List<String>> response = new HashMap<>();
        response.put(keyword, images);

        return ResponseEntity.status(200).body(response);
    }

}
