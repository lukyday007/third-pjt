package com.singlebungle.backend.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.global.exception.InvalidApiUrlException;
import com.singlebungle.backend.global.exception.InvalidResponseException;
import com.singlebungle.backend.global.exception.UnAuthorizedApiKeyException;
import com.singlebungle.backend.global.model.BaseResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
            log.info(">>> ChatGPT Request JSON: {}", jsonRequest);

            ChatGPTResponse response = openAiConfig.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatGPTResponse.class)
                    .block();

            /* ==================================================

            if (response != null && !response.getChoices().isEmpty()) {
                String resultContent = response.getChoices().get(0).getMessage().getContent();
                log.info(">>> OpenAI Response: {}", resultContent);

                return BaseResponseBody.of(200, resultContent);
            } else {
                return BaseResponseBody.of(500, "응답을 처리할 수 없습니다.");
            }

            =================================================  */

            if (response == null) {
                log.error(">>> OpenAI Response: 응답이 null입니다.");
                throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 null 응답을 반환했습니다.");
            }

            if (response.getChoices().isEmpty()) {
                log.error(">>> OpenAI Response: choices가 비어 있습니다.");
                throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 빈 응답을 반환했습니다.");
            }

            // choices의 첫 번째 요소 가져오기
            ChatGPTResponse.Choice choice = response.getChoices().get(0);
            if (choice.getMessage() == null || choice.getMessage().getContent() == null) {
                log.error(">>> OpenAI Response: 응답 메시지가 없습니다.");
                throw new InvalidResponseException("응답을 처리할 수 없습니다. 응답 메시지가 없습니다.");
            }

            String resultContent = choice.getMessage().getContent();
            log.info(">>> OpenAI Response: {}", resultContent);

            return BaseResponseBody.of(200, resultContent);


        } catch (WebClientRequestException e) {
            throw new InvalidApiUrlException(">>> ChatGPT api url이 부정확합니다. 확인해주세요.");

        } catch (WebClientResponseException.Unauthorized e) {
            throw new UnAuthorizedApiKeyException(">>> ChatGPT api 인증이 실패했습니다. api 키를 확인해주세요.");

        } catch (Exception e) {
            log.error(">>> Image Analysis Failed: {}", e.getMessage(), e);

            return BaseResponseBody.of(500, "이미지 분석에 오류가 발생했습니다.");
        }
    }
}

