package com.singlebungle.backend.domain.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatGPTResponse {

    private String id;
    private String object;
    private int created;
    private List<Choice> choices;
    private Usage usage;

    @Getter
    public static class Choice {
        private int index;
        private Message message;
        private String finish_reason;
    }

    @Getter
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
