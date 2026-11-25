package com.example.techstore.exception;

public class DiscountStateException extends ConflictException {
    public DiscountStateException(ErrorCode code, String message) {
        super(code, message);
    }
}
