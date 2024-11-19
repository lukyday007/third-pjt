package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class InvalidApiUrlException extends RuntimeException {
    private final String message;

    public InvalidApiUrlException() {
        this.message = "api url이 부정확합니다. 확인해주세요.";
    }

    public InvalidApiUrlException(String message) {
        this.message = message;
    }

}
