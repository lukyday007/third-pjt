package com.singlebungle.backend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Configuration
public class GoogleVisionConfig {

    @Value("${google.vision.credential-json}")
    private String googleCredentialJson;

    public GoogleCredentials getGoogleCredentials() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(googleCredentialJson.getBytes()));

        return credentials;
    }

}