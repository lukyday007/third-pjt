package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final String message;

    public InvalidRequestException() {
      this.message = "클라이언트 요청이 적합하지 않습니다.";
    }

    public InvalidRequestException(String message) {
        this.message = message;
    }
}
