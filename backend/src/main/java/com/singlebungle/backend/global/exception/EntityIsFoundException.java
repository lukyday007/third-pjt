package com.singlebungle.backend.global.exception;

import lombok.Getter;

@Getter
public class EntityIsFoundException extends RuntimeException {
    private final String message;

    public EntityIsFoundException() {
        this.message = "해당하는 데이터가 이미 존재합니다.";
    }

    public EntityIsFoundException(String message) {
        this.message = message;
    }
}
