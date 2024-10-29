package com.singlebungle.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.url}")
    private String apiUrl;

    // Todo : openai api
    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + openAiApiKey);
    }

    @Bean
    public WebClient openAiWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.build();
    }


}
