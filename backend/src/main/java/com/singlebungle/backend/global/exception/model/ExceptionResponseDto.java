package com.singlebungle.backend.global.exception.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExceptionResponseDto {
    private String httpMethod;
    private String requestURL;
    private int httpStatus;
    private String message;
    private LocalDateTime timestamp;

    public ExceptionResponseDto(String httpMethod, String requestURL, int httpStatus, String message, LocalDateTime timestamp) {
        this.httpMethod = httpMethod;
        this.requestURL = requestURL;
        this.httpStatus = httpStatus;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static ExceptionResponseDto of(String httpMethod, String requestURL, int httpStatus, String message) {
        return new ExceptionResponseDto(httpMethod, requestURL, httpStatus, message, LocalDateTime.now());
    }
}
