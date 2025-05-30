package com.singlebungle.backend.domain.keyword.service;

import com.singlebungle.backend.domain.keyword.dto.KeywordRankResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface KeywordService {

    void saveKeyword(List<String> keywords);

    void increaseCurCnt(String keyword);

    List<KeywordRankResponseDTO> getKeywordRankList();

    void updateKeywordRanking();

    void updateSQLDB();

    Map<String, Integer> createRankMap(List<String> ranks);

    List<String> getKeywords(String token, String keyword, Long directoryId, boolean bin);
}
