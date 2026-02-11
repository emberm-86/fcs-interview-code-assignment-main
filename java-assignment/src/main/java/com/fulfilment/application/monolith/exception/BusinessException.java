package com.fulfilment.application.monolith.exception;

public class BusinessException extends RuntimeException {
    String message;
    ErrorType errorType;

    public BusinessException(String message, ErrorType errorType) {
        super(message);
        this.message = message;
        this.errorType = errorType;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
