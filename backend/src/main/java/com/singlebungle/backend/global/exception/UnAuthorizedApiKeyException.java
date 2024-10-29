package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class UnAuthorizedApiKeyException extends RuntimeException {

    private final String message;

    public UnAuthorizedApiKeyException() {
        this.message = "api 인증에 실패했습니다.";
    }

    public UnAuthorizedApiKeyException(String message) {
        this.message = message;
    }

}
