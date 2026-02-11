package com.fulfilment.application.monolith.exception;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message, ErrorType.VALIDATION);
    }
}
