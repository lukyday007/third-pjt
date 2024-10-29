package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class ImageSaveException extends RuntimeException  {
    private final String message;

    public ImageSaveException() {
        this.message = "이미지 저장에 실패했습니다.";
    }

    public ImageSaveException(String message) {
        this.message = message;
    }
}
