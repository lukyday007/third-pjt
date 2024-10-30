package com.singlebungle.backend.domain.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.image.service.ImageService;
import com.singlebungle.backend.domain.keyword.service.KeywordService;
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

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class OpenaiServiceImpl implements OpenaiService {

    private final WebClient openAiConfig;  // WebClient 빈을 openAiConfig로 주입받음

    private List<String> keywords;

    @Value("${openai.model}")
    private String apiModel;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Override
    public List<String> requestImageAnalysis(String imageUrl, List<String> labels) {
        try {
            // 프롬프트 생성
            String gptPrompt = generatePrompt(imageUrl, labels);

            // OpenAI api 요청
            ChatGPTResponse response = sendOpenAiRequest(imageUrl, gptPrompt);

            // 응답 처리 및 키워드 추출
            String resultContent = extractResponseContent(response);
            List<String> keywords = extractKeywords(resultContent);

            return keywords;


        } catch (WebClientRequestException e) {
            throw new InvalidApiUrlException(">>> ChatGPT api url이 부정확합니다. 확인해주세요.");

        } catch (WebClientResponseException.Unauthorized e) {
            throw new UnAuthorizedApiKeyException(">>> ChatGPT api 인증이 실패했습니다. api 키를 확인해주세요.");

        } catch (Exception e) {
            log.error(">>> Image Analysis Failed: {}", e.getMessage(), e);
            throw new RuntimeException(">>> api 요청 중 오류가 발생했습니다.");
        }
    }


    // 1. 프롬프트 생성 메서드
    @Override
    public String generatePrompt(String imageUrl, List<String> labels) {
        String labelsToString = String.join(", ", labels);

        return String.format(
                "### 라벨 번역\n" +
                        "우선 labels 배열에 있는 목록을 명사로 번역해서 리스트로 돌려줘 [%s].\n" +
                        "이때 들여쓰기하고 번역한 결과를 써야해.\n\n" +
                        "### 이미지에 있는 텍스트\n" +
                        "이미지에 텍스트가 있으면 텍스트를 학습하고 반환해. 없으면 '텍스트 없음'이라고 작성해.\n\n" +
                        "### 키워드\n" +
                        "이 이미지에 대한 키워드를 5개 정도 추출해서 번호와 함께 리스트로 반환해줘. 이때 '텍스트' 나 '언어', '유머', '인물'은 없는 키워드여야 해",
                labelsToString
        );
    }


    // 2. API 요청 메서드
    @Override
    public ChatGPTResponse sendOpenAiRequest(String imageUrl, String prompt) throws Exception {
        ChatGPTRequest request = ChatGPTRequest.builder()
                .model(apiModel)
                .messages(List.of(
                        ChatGPTRequest.UserMessage.builder()
                                .role("user")
                                .content(List.of(
                                        new ChatGPTRequest.Content("text", prompt),
                                        new ChatGPTRequest.Content("image_url", new ChatGPTRequest.ImageUrl(imageUrl))
                                ))
                                .build()
                ))
                .maxTokens(500)
                .temperature(0)
                .build();

        log.info(">>> ChatGPT Request JSON: {}", new ObjectMapper().writeValueAsString(request));

        return openAiConfig.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .block();
    }


    // 3. 응답 내용 추출 메서드
    @Override
    public String extractResponseContent(ChatGPTResponse response) {
        if (response == null || response.getChoices().isEmpty()) {
            if (response == null)
                log.error(">>> OpenAI Response: 응답이 null입니다.");
            if (response.getChoices().isEmpty())
                log.error(">>> OpenAI Response: choices가 비어 있습니다.");

            throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 빈 응답을 반환했습니다.");
        }

        ChatGPTResponse.Choice choice = response.getChoices().get(0);
        String resultContent = choice.getMessage().getContent();
        log.info(">>> OpenAI Response: {}", resultContent);

        return resultContent;
    }


    // 4. 키워드 추출 메서드
    @Override
    public List<String> extractKeywords(String resultContent) {
        List<String> keywords = new ArrayList<>();

        // "### 키워드" 이후의 항목만 추출하는 정규 표현식
        Pattern pattern = Pattern.compile("### 키워드\\s*([\\s\\S]+)");
        Matcher matcher = pattern.matcher(resultContent);

        if (matcher.find()) {
            // 키워드 섹션만 추출
            String keywordSection = matcher.group(1);

            // 각 항목을 줄 단위로 분리하여 리스트에 추가
            String[] lines = keywordSection.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d+\\.\\s*(.+)")) {
                    // "키워드" 형식에 맞는 항목만 추출
                    keywords.add(line.replaceAll("\\d+\\.\\s*", ""));
                }
            }
        }

        return keywords;
    }


}