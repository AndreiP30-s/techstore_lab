package com.example.techstore.exception;

public abstract class ApiException extends RuntimeException {
    private final ErrorCode code;
    private final String hint;

    protected ApiException(ErrorCode code, String message) {
        this(code, message, code.getHint());
    }

    protected ApiException(ErrorCode code, String message, String hint) {
        super(message);
        this.code = code;
        this.hint = hint;
    }

    protected ApiException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.hint = code.getHint();
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getHint() {
        return hint;
    }
}
