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
            if (jpegBytes.length > 15 * 1024 * 1024) { // 예: 15MB 초과 제한
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

        return String.format(
                "I am working on a project to analyze images. Please analyze the given image based on the provided labels. The output should strictly follow the instructions below:\n\n" +
                        "### Labels\n" +
                        "Translate the following labels into Korean nouns. The result should be returned as a list, and each translated noun should be on a separate line:\n" +
                        "[%s]\n\n" +
                        "Example:\n" +
                        "- 강아지\n" +
                        "- 고양이\n" +
                        "- 풍경\n\n" +
                        "### Keywords\n" +
                        "Extract exactly 5 main keywords related to this image and return them as a numbered list. The keywords should be one-word Korean nouns without any modifiers (e.g., no adjectives or adverbs).\n\n" +
                        "Example:\n" +
                        "1. 동물\n" +
                        "2. 자연\n" +
                        "3. 가족\n" +
                        "4. 여행\n" +
                        "5. 사진\n\n" +
                        "All responses must be grammatically correct in Korean.",
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
        // 첫 번째 choice 가져오기
        ChatGPTResponse.Choice choice = response.getChoices().get(0);

        // choice가 null이거나 message가 null인지 확인
        if (choice == null || choice.getMessage() == null) {
            log.error(">>> OpenAI 응답에서 choice나 message가 null입니다: {}", choice);
            throw new InvalidResponseException("응답을 처리할 수 없습니다. choice나 message가 null입니다.");
        }

        // message content 가져오기
        String content = choice.getMessage().getContent();

        // content가 null인지 확인
        if (content == null || content.isEmpty()) {
            log.warn(">>> OpenAI 응답에서 content가 비어 있습니다: {}", choice);
            throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 빈 content를 반환했습니다.");
        }

        // 정상적인 content 반환
        log.info(">>> OpenAI 응답에서 추출된 content: {}", content);
        return content;
    }


    // 4. 키워드 추출 메서드
    @Override
    public List<String> extractKeywords(String resultContent) {
        List<String> keywords = new ArrayList<>();

        // "### 키워드" 이후의 항목만 추출하는 정규 표현식
        Pattern pattern = Pattern.compile("### Keywords\\s*([\\s\\S]+)");
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
        Pattern pattern = Pattern.compile("### Labels\\s*([\\s\\S]+?)\\n\\n###");
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
