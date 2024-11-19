package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {
    private final String message;

    public EntityNotFoundException() {
        this.message = "해당하는 데이터를 찾을 수 없습니다.";
    }

    public EntityNotFoundException(String message) {
        this.message = message;
    }
}
