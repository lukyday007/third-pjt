package com.singlebungle.backend.domain.search.controller;

import com.singlebungle.backend.domain.keyword.service.KeywordService;
import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.global.model.BaseResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/{keyword}/increment")
    public ResponseEntity<BaseResponseBody> incrementSearchCount(@RequestParam String keyword) {

        searchService.incrementSearchCount(keyword);
        return ResponseEntity.status(201).body(BaseResponseBody.of(201, "해당 키워드의 조회수가 증가했습니다."));
    }
}