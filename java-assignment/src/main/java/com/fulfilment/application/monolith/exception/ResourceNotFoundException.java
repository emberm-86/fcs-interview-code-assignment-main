package com.fulfilment.application.monolith.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorType.NOT_FOUND);
    }
}
