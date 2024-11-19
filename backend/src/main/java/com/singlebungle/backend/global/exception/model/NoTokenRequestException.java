package com.singlebungle.backend.global.exception.model;

import lombok.Getter;

// 요청을 처리할 권한이 없을 경우의 예외
@Getter
public class NoTokenRequestException extends RuntimeException {
    private final String message;

    public NoTokenRequestException() {
        this.message = "Access 토큰이 없습니다.";
    }

    public NoTokenRequestException(String message) {
        this.message = message;
    }
}
