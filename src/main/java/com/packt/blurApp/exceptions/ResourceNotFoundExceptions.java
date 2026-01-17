package com.packt.blurApp.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundExceptions extends ApiException {
    public ResourceNotFoundExceptions(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
