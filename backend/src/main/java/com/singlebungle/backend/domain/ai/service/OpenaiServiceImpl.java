package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
import com.singlebungle.backend.domain.search.service.SearchService;
import com.singlebungle.backend.global.exception.InvalidApiUrlException;
import com.singlebungle.backend.global.exception.InvalidResponseException;
import com.singlebungle.backend.global.exception.UnAuthorizedApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
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

    public List<String> tags;

    @Value("${openai.model}")
    private String apiModel;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Override
    public KeywordAndLabels requestImageAnalysis(String imageUrl, List<String> labels) {
        try {

            // WebP 처리 (URL 이미지)
            if (imageUrl.endsWith(".webp")) {
                log.info(">>> WebP 이미지 감지, 변환 중...");
                imageUrl = convertWebPToJpegUrl(imageUrl);
            }

            // 프롬프트 생성
            String gptPrompt = generatePrompt(imageUrl, labels);
            // OpenAI api 요청
            ChatGPTResponse response = sendOpenAiRequest(imageUrl, gptPrompt);
            // 응답 처리
            String resultContent = extractResponseContent(response);
            // 키워드 추출
            List<String> keywords = extractKeywords(resultContent);
            // 태그 추출
            tags = extractTags(resultContent);


            return new KeywordAndLabels(keywords, tags);

        } catch (WebClientRequestException e) {
            throw new InvalidApiUrlException(">>> ChatGPT api url이 부정확합니다. 확인해주세요. : " + e  );

        } catch (WebClientResponseException.Unauthorized e) {
            throw new UnAuthorizedApiKeyException(">>> ChatGPT api 인증이 실패했습니다. api 키를 확인해주세요.");

        } catch (Exception e) {
            log.error(">>> Image Analysis Failed: {}", e.getMessage(), e);
            throw new RuntimeException(">>> api 요청 중 오류가 발생했습니다.");
        }
    }

    private String convertWebPToJpegUrl(String webpUrl) throws IOException {
        try {
            // WebP 이미지 다운로드
            BufferedImage webpImage = ImageIO.read(new URL(webpUrl));
            if (webpImage == null) {
                log.error(">>> WebP 이미지를 읽을 수 없습니다. URL: {}", webpUrl);
                throw new IllegalArgumentException("WebP 이미지를 읽는 데 실패했습니다.");
            }

            // JPEG로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(webpImage, "jpg", outputStream);
            byte[] jpegBytes = outputStream.toByteArray();

            // 변환된 JPEG 이미지의 크기를 검증
            if (jpegBytes.length == 0) {
                log.error(">>> JPEG 변환 실패: 변환된 이미지 크기가 0입니다.");
                throw new RuntimeException("JPEG 변환 실패: 변환된 이미지 크기가 0입니다.");
            }
            if (jpegBytes.length > 5 * 1024 * 1024) { // 예: 5MB 초과 제한
                log.warn(">>> JPEG 이미지가 너무 큽니다. 크기: {} bytes", jpegBytes.length);
                throw new IllegalArgumentException("JPEG 이미지 크기가 너무 큽니다.");
            }

            // Base64로 변환하여 OpenAI에 전달
            String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(jpegBytes);
            log.info(">>> WebP 이미지를 JPEG(Base64)로 변환 완료, Base64 길이: {}", base64Image.length());

            return base64Image;

        } catch (IOException e) {
            log.error(">>> WebP 이미지를 JPEG로 변환하는 동안 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("WebP 이미지를 JPEG로 변환하는 동안 오류가 발생했습니다.", e);
        }
    }


    // 1. 프롬프트 생성 메서드
    @Override
    public String generatePrompt(String imageUrl, List<String> labels) {
        String labelsToString = String.join(", ", labels);

//        "### 이미지에 있는 텍스트\n" +
//                "이미지에 텍스트가 있으면 텍스트를 학습해. 없으면 '텍스트 없음'이라고 작성해.\n\n" +

        return String.format(
                "### 라벨 번역\n" +
                        "우선 labels 배열에 있는 목록을 명사로 번역해서 리스트로 돌려줘 [%s].\n" +
                        "이때 들여쓰기하고 번역한 결과를 써야해.\n\n" +
                        "### 키워드\n" +
                        "이 이미지에 대한 키워드를 5개 정도 추출해서 번호와 함께 리스트로 반환해줘. 이때 '텍스트' 나 '언어', '유머', '인물', '밈'은 없는 키워드여야 하고, 명사형에 한 단어어야해.",
                labelsToString
        );
    }


    // 2. API 요청 메서드
    @Override
    public ChatGPTResponse sendOpenAiRequest(String imageUrl, String prompt) throws Exception {
        try {
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

            return openAiConfig.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatGPTResponse.class)
                    .block();

        } catch (Exception e) {
            log.error(">>> OpenAI API 요청 실패: {}", e.getMessage());
            throw new RuntimeException(">>> OpenAI API 요청 중 오류가 발생했습니다.");
        }
    }


    // 3. 응답 내용 추출 메서드
    @Override
    public String extractResponseContent(ChatGPTResponse response) {
        if (response == null || response.getChoices().isEmpty()) {
            log.error(">>> OpenAI 응답이 비어 있습니다.");
            throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 빈 응답을 반환했습니다.");
        }

        ChatGPTResponse.Choice choice = response.getChoices().get(0);
        return choice.getMessage().getContent();
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


    @Override
    public List<String> extractTags(String response) {
        List<String> labels = new ArrayList<>();

        // "### 라벨 번역" 이후의 항목만 추출하는 정규 표현식
        Pattern pattern = Pattern.compile("### 라벨 번역\\s*([\\s\\S]+?)\\n\\n###");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // 라벨 섹션만 추출
            String labelSection = matcher.group(1);

            // 각 항목을 줄 단위로 분리하여 리스트에 추가
            String[] lines = labelSection.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {  // 빈 줄 제외
                    labels.add(line.replaceFirst("-\\s*", ""));  // "-" 문자 제거
                }
            }
        }

        return labels;
    }

}