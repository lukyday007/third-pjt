package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class UrlAccessException extends RuntimeException {
    private final String message;

    public UrlAccessException() {
        this.message = "요청하신 url에 접근이 불가합니다.";
    }

    public UrlAccessException(String message) {
        this.message = message;
    }
}
