package com.example.techstore.exception;

public class PriceCalculationException extends ApiException {
    public PriceCalculationException(String message, Throwable cause) {
        super(ErrorCode.PRICE_CALCULATION_FAILED, message, cause);
    }
}
