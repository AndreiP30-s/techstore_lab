package com.example.techstore.exception;

public class InvalidCharacteristicException extends ValidationException {
    public InvalidCharacteristicException(ErrorCode code, String message) {
        super(code, message);
    }

    public InvalidCharacteristicException(ErrorCode code, String message, String hint) {
        super(code, message, hint);
    }
}
