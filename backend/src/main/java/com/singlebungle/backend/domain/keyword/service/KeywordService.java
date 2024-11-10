package com.singlebungle.backend.domain.keyword.service;

import org.springframework.stereotype.Service;

import java.util.List;

public interface KeywordService {

    void saveKeyword(List<String> keywords);

    void increaseCurCnt(String keyword);
}
