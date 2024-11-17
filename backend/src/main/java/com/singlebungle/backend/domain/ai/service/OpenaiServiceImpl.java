package com.singlebungle.backend.domain.ai.service;

import com.singlebungle.backend.domain.ai.dto.request.ChatGPTRequest;
import com.singlebungle.backend.domain.ai.dto.response.ChatGPTResponse;
import com.singlebungle.backend.domain.ai.dto.response.KeywordAndLabels;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class OpenaiServiceImpl implements OpenaiService {

    private final WebClient openAiConfig;  // WebClient 빈을 openAiConfig로 주입받음
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // 병렬 처리를 위한 스레드풀 생성

    @Value("${openai.model}")
    private String apiModel;

    @Override
    public KeywordAndLabels requestImageAnalysis(String imageUrl, List<String> labels) {
        try {
            final String imageUrlStr = imageUrl;

            // WebP 처리 (URL 이미지)
            if (imageUrl.endsWith(".webp")) {
                log.info(">>> WebP 이미지 감지, 변환 중...");
                imageUrl = convertWebPToJpegUrl(imageUrl);
            }

            // OpenAI API 요청: Keywords 추출 -> 비동기
            CompletableFuture<List<String>> keywordsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return requestKeywords(imageUrlStr);
                } catch (Exception e) {
                    log.warn(">>> Keywords 분석 실패: {}", e.getMessage());
                    return null; // 실패 시 null 반환
                }
            }, executorService);

            // OpenAI API 요청: Labels 번역 -> 비동기
            CompletableFuture<List<String>> labelsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return requestLabels(labels);
                } catch (Exception e) {
                    log.warn(">>> Labels 번역 실패: {}", e.getMessage());
                    return null; // 실패 시 null 반환
                }
            }, executorService);

            // 두 비동기 작업이 완료될 때까지 기다림
            CompletableFuture.allOf(keywordsFuture, labelsFuture).join();

            // 결과 가져오기
            List<String> keywords = keywordsFuture.join();
            List<String> tags = labelsFuture.join();

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

    private List<String> requestKeywords(String imageUrl) throws Exception {
        String gptPromptForKeywords = generateKeywordsPrompt(imageUrl);
        ChatGPTResponse keywordsResponse = sendOpenAiRequest(imageUrl, gptPromptForKeywords);
        String keywordsContent = extractResponseContent(keywordsResponse);
        return extractKeywords(keywordsContent); // 키워드 추출
    }

    private List<String> requestLabels(List<String> labels) throws Exception {
        String gptPromptWithLabels = generateLabelsPrompt(labels);
        ChatGPTResponse labelsResponse = sendOpenAiRequest(null, gptPromptWithLabels);
        String labelsContent = extractResponseContent(labelsResponse);
        return extractLabels(labelsContent); // 라벨 번역
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
    public String generateLabelsPrompt(List<String> labels) {
        String labelsToString = String.join(", ", labels);

        return String.format(
                "Please translate the following labels into one-word Korean nouns, avoiding repetitive or closely related words (e.g., synonyms, category names, or overly broad terms). Return only the top 5 distinct and meaningful labels, prioritizing uniqueness and specificity. The response **must** be returned in the following format:\n\n" +
                        "### Labels\n" +
                        "[%s]\n\n" +
                        "Example:\n" +
                        "- word\n" +
                        "- word\n" +
                        "- word\n\n" +
                        "Rules:\n" +
                        "1. Ensure that each word is unique and distinct in its meaning.\n" +
                        "2. Only include nouns.\n" +
                        "All responses must be grammatically correct in Korean.",
                labelsToString
        );
    }

    @Override
    public String generateKeywordsPrompt(String imageUrl) {
        return String.format(
                "아래 지침을 엄격히 따라주세요:\n\n" +
                        "### Keywords\n" +
                        "이 이미지와 관련된 주요 키워드 5개를 목록으로 반환해주세요. 키워드는 반드시 수식어(예: 형용사, 부사)가 없는 한 단어의 한국어 명사여야 합니다.\n\n" +
                        "예시:\n" +
                        "- 동물\n" +
                        "- 자연\n" +
                        "- 가족\n" +
                        "- 여행\n" +
                        "- 사진\n\n" +
                        "응답 형식:\n" +
                        "'### Keywords' 아래에 지정된 형식에 따라 키워드를 반환하세요. 모든 응답은 반드시 문법적으로 올바르고 한국어로 작성되어야 합니다.",

                imageUrl
        );
    }


    // 2. API 요청 메서드
    @Override
    public ChatGPTResponse sendOpenAiRequest(String imageUrl, String prompt) throws Exception {
        try {
            // 요청 생성 메서드 호출
            ChatGPTRequest request = createChatGPTRequest(prompt, imageUrl);

            // OpenAI API 호출
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


    private ChatGPTRequest createChatGPTRequest(String prompt, String imageUrl) {
        List<ChatGPTRequest.Content> contents = new ArrayList<>();
        contents.add(new ChatGPTRequest.Content("text", prompt));

        if (imageUrl != null) { // 이미지 URL이 존재하면 추가
            contents.add(new ChatGPTRequest.Content("image_url", new ChatGPTRequest.ImageUrl(imageUrl)));
        }

        return ChatGPTRequest.builder()
                .model(apiModel) // 모델 설정
                .messages(List.of(
                        ChatGPTRequest.UserMessage.builder()
                                .role("user")
                                .content(contents)
                                .build()
                ))
                .maxTokens(800) // 기본 설정
                .temperature(0)
                .build();
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

        // message content 가져오기
        String content = choice.getMessage().getContent();

        // content가 null인지 확인
        if (content == null || content.isEmpty()) {
            log.warn(">>> OpenAI 응답에서 content가 비어 있습니다: {}", choice);
            throw new InvalidResponseException("응답을 처리할 수 없습니다. OpenAI에서 빈 content를 반환했습니다.");
        }

//        // 정상적인 content 반환
//        log.info(">>> OpenAI 응답에서 추출된 content: {}", content);
        return content;
    }


    // 4. 키워드 추출 메서드
    @Override
    public List<String> extractKeywords(String resultContent) {
        List<String> keywords = new ArrayList<>();

        // "### 키워드" 이후의 항목만 추출하는 정규 표현식
        Pattern pattern = Pattern.compile("### Keywords\\s*([\\s\\S]+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(resultContent);

        if (matcher.find()) {
            // "### Labels" 이후의 내용 추출
            String labelSection = matcher.group(1);

            // 각 줄을 처리하여 라벨 리스트 생성
            String[] lines = labelSection.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && line.startsWith("-")) { // "-"로 시작하는 유효한 라인만 처리
                    keywords.add(line.replaceFirst("-\\s*", "")); // "-" 문자 제거 후 리스트에 추가
                }
            }
        }

        return keywords;
    }


    @Override
    public List<String> extractLabels(String response) {
        List<String> labels = new ArrayList<>();

        // "### Labels" 이후의 모든 항목 추출
        Pattern pattern = Pattern.compile("### Labels\\s*([\\s\\S]+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // "### Labels" 이후의 내용 추출
            String labelSection = matcher.group(1);

            // 각 줄을 처리하여 라벨 리스트 생성
            String[] lines = labelSection.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && line.startsWith("-")) { // "-"로 시작하는 유효한 라인만 처리
                    labels.add(line.replaceFirst("-\\s*", "")); // "-" 문자 제거 후 리스트에 추가
                }
            }
        }

        // 결과 출력
        log.info(">>> 추출된 라벨: {}", labels);

        return labels;
    }

}
