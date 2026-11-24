package com.example.techstore.exception;

public class ConflictException extends ApiException {
    public ConflictException(ErrorCode code, String message) {
        super(code, message);
    }
}
