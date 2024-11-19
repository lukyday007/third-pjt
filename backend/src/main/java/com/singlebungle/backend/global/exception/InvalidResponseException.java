package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class InvalidResponseException extends RuntimeException {
    private final String message;

    public InvalidResponseException() {
        this.message = "응답을 처리할 수 없습니다.";
    }

    public InvalidResponseException(String messgae) {
        this.message = messgae;
    }

}
