package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
import com.singlebungle.backend.domain.ai.dto.response.KeywordsFromOpenAi;

import java.io.IOException;
import java.util.List;


public interface OpenaiService {

    KeywordAndLabels requestImageAnalysis(String imageUrl, List<String> labels);

    List<String> requestKeywords(String imageUrl) throws Exception;

    List<String> requestLabels(List<String> labels) throws Exception;

    String convertWebPToJpegUrl(String webpUrl) throws IOException;

    // 프롬프트 생성
    String generateKeywordsPrompt(String imageUrl);
    String generateLabelsPrompt(List<String> labels);
    ChatGPTResponse sendOpenAiRequest(String imageUrl, String prompt) throws Exception;

    ChatGPTRequest createChatGPTRequest(String prompt, String imageUrl);

    // 응답 처리 및 키워드 추출
    String extractResponseContent(ChatGPTResponse response);
    List<String> extractLabels(String response);
    List<String> extractKeywords(String response);



}
