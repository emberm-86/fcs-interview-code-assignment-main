package com.fulfilment.application.monolith.exception;

public class ResourceConflictException extends BusinessException{
    public ResourceConflictException(String message) {
        super(message, ErrorType.CONFLICT);
    }
}
