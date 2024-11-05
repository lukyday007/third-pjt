package com.singlebungle.backend.domain.keyword.controller;

import com.singlebungle.backend.domain.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keyword")
public class KeywordController {

    private final SearchService searchService;

    @PostMapping("/{keyword}")
    @Operation(summary = "키워드 검색", description = "키워드를 검색합니다.")
    public ResponseEntity<Map<String, List<String>>> search(
            @PathVariable String keyword
    ) {
        List<String> images = searchService.getImageUrlsByTag(keyword);

        Map<String, List<String>> response = new HashMap<>();
        response.put(keyword, images);

        return ResponseEntity.status(200).body(response);
    }

}
