package com.packt.blurApp.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String error;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.error = status.getReasonPhrase();
    }

    public ApiException(String message, HttpStatus status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public ApiException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.error = status.getReasonPhrase();
    }
}
