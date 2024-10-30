package com.singlebungle.backend.global.exception.handler;

import com.singlebungle.backend.global.exception.*;
import com.singlebungle.backend.global.exception.model.ExceptionResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityIsFoundException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ExceptionResponseDto> handleEntityIsFoundException(EntityIsFoundException ex, HttpServletRequest request) {

        log.error("EntityIsFoundException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionResponseDto> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {

        log.error("EntityNotFoundException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponseDto> handleInvalidImageException(InvalidImageException ex, HttpServletRequest request) {

        log.error("InvalidImageException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InvalidApiUrlException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionResponseDto> handleInvalidApiUrlException(
            InvalidApiUrlException ex, HttpServletRequest request) {

        log.error("InvalidApiUrlException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(UnAuthorizedApiKeyException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ExceptionResponseDto> handleUnAuthorizedApiKeyException(
            UnAuthorizedApiKeyException ex, HttpServletRequest request) {

        log.error("UnAuthorizedApiKeyException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(InvalidResponseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionResponseDto> handleInvalidResponseException(
            InvalidResponseException ex, HttpServletRequest request) {

        log.error("InvalidResponseException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ImageSaveException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionResponseDto> handleImageSaveException(
            ImageSaveException ex, HttpServletRequest request) {

        log.error("ImageSaveException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionResponseDto> handleInvalidRequestException(
            InvalidRequestException ex, HttpServletRequest request) {

        log.error("InvalidRequestException 발생 - URL: {}, Message: {}", request.getRequestURI(), ex.getMessage());

        ExceptionResponseDto response = ExceptionResponseDto.of(
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}