package com.example.techstore.exception;

public class ValidationException extends ApiException {
    public ValidationException(ErrorCode code, String message) {
        super(code, message);
    }

    public ValidationException(ErrorCode code, String message, String hint) {
        super(code, message, hint);
    }
}
