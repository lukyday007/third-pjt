package com.singlebungle.backend.global.exception;

import lombok.Getter;

import java.io.IOException;

@Getter
public class InvalidImageException extends IOException {

    private final String message;

    public InvalidImageException() {
        this.message = "부적절한 이미지입니다.";
    }

    public InvalidImageException(String message) {
        this.message = message;
    }

}
