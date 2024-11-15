package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.ai.dto.response.KeywordsFromOpenAi;
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
    public KeywordsFromOpenAi requestImageAnalysis(String imageUrl, boolean result) {
        try {

            // WebP 처리 (URL 이미지)
            if (imageUrl.endsWith(".webp")) {
                log.info(">>> WebP 이미지 감지, 변환 중...");
                imageUrl = convertWebPToJpegUrl(imageUrl);
            }

            // 프롬프트 생성
            String gptPrompt = generatePrompt(imageUrl, result);
            // OpenAI api 요청
            ChatGPTResponse response = sendOpenAiRequest(imageUrl, gptPrompt);
            // 응답 처리
            String resultContent = extractResponseContent(response);
            // 키워드 추출
            List<String> keywords = extractKeywords(resultContent);
            // 태그 추출
            tags = extractTags(resultContent);


            return new KeywordsFromOpenAi(keywords);

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
    public String generatePrompt(String imageUrl, boolean result) {

        return String.format(
                "아래 지침을 엄격히 따라주세요:\n\n" +
                        "### 키워드\n" +
                        "이 이미지와 관련된 주요 키워드 5개를 추출하여 번호가 매겨진 목록으로 반환해주세요. 키워드는 반드시 수식어(예: 형용사, 부사)가 없는 한 단어의 한국어 명사여야 합니다.\n\n" +
                        "예시:\n" +
                        "1. 동물\n" +
                        "2. 자연\n" +
                        "3. 가족\n" +
                        "4. 여행\n" +
                        "5. 사진\n\n" +
                        "응답 형식:\n" +
                        "'### 라벨' 아래에 라벨을 번역하여 반환하고, '### 키워드' 아래에 지정된 형식에 따라 키워드를 반환하세요. 모든 응답은 반드시 문법적으로 올바르고 한국어로 작성되어야 합니다."
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
