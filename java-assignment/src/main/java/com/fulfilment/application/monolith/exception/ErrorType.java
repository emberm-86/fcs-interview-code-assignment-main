package com.fulfilment.application.monolith.exception;

public enum ErrorType {
    VALIDATION(422),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_SERVER_ERROR(500);

    private final int statusCode;

    ErrorType(int statusCode) {
        this.statusCode = statusCode;
    }

    public int status() {
        return statusCode;
    }
}
