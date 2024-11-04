package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
import com.singlebungle.backend.global.model.BaseResponseBody;

import java.util.List;


public interface OpenaiService {

    KeywordAndLabels requestImageAnalysis(String imageUrl, List<String> labels);
    // 프롬프트 생성
    String generatePrompt(String imageUrl, List<String> labels);
    // OpenAI api 요청
    ChatGPTResponse sendOpenAiRequest(String imageUrl, String prompt) throws Exception;
    // 응답 처리 및 키워드 추출
    String extractResponseContent(ChatGPTResponse response);
    List<String> extractTags(String response);
    List<String> extractKeywords(String response);



}
