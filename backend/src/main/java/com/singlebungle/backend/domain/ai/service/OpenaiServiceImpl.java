package com.singlebungle.backend.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.global.model.BaseResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class OpenaiServiceImpl implements OpenaiService {

    private final WebClient openAiConfig;  // WebClient 빈을 openAiConfig로 주입받음

    @Value("${openai.model}")
    private String apiModel;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    public BaseResponseBody requestImageAnalysis(String imageUrl, String requestText) {

        ObjectMapper objectMapper = new ObjectMapper();

        // ChatGPTRequest 객체 생성
        ChatGPTRequest request = ChatGPTRequest.builder()
                .model(apiModel)
                .messages(List.of(
                        ChatGPTRequest.UserMessage.builder()
                                .role("user")
                                .content(List.of(
                                        new ChatGPTRequest.Content("text", requestText),  // 텍스트 전용 생성자 사용
                                        new ChatGPTRequest.Content("image_url", new ChatGPTRequest.ImageUrl(imageUrl))  // 이미지 URL 전용 생성자 사용
                                ))
                                .build()
                ))
                .maxTokens(500)
                .temperature(0)
                .build();

        try {
            String jsonRequest = objectMapper.writeValueAsString(request);
            log.info("Request JSON: {}", jsonRequest);

            ChatGPTResponse response = openAiConfig.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatGPTResponse.class)
                    .block();

            if (response != null && !response.getChoices().isEmpty()) {
                String resultContent = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI Response: {}", resultContent);
                return BaseResponseBody.of(200, resultContent);
            } else {
                return BaseResponseBody.of(500, "응답을 처리할 수 없습니다.");
            }

        } catch (Exception e) {
            log.error("Image Analysis Failed: {}", e.getMessage(), e);
            return BaseResponseBody.of(500, "이미지 분석에 오류가 발생했습니다.");
        }
    }
}

