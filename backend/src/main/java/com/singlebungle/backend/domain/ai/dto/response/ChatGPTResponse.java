package com.singlebungle.backend.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatGPTResponse {

    private String id;
    private String object;
    private int created;
    private List<Choice> choices;
    private Usage usage;

    @ToString
    @Getter
    public static class Choice {
        private int index;
        private Message message;
        private String finish_reason;
    }

    @ToString
    @Getter
    public static class Message {
        private String role;
        private String content;
    }

    @ToString
    @Getter
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
