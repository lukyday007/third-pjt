package com.singlebungle.backend.domain.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatGPTRequest {

    @JsonProperty("model")
    private String model;

    private List<UserMessage> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private int temperature;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserMessage {
        private String role;
        private List<Content> content;
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)  // null인 필드는 JSON에 포함되지 않도록 설정
    @NoArgsConstructor
    public static class Content {
        private String type;
        private String text;

        @JsonProperty("image_url")
        private ImageUrl imageUrl;

        // text 타입일 때만 text 필드를 초기화하는 생성자
        public Content(String type, String text) {
            this.type = type;
            this.text = text;
        }

        // image_url 타입일 때만 imageUrl 필드를 초기화하는 생성자
        public Content(String type, ImageUrl imageUrl) {
            this.type = type;
            this.imageUrl = imageUrl;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageUrl {
        private String url;
    }
}