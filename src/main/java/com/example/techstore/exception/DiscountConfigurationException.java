package com.example.techstore.exception;

public class DiscountConfigurationException extends ApiException {
    public DiscountConfigurationException() {
        super(ErrorCode.DISCOUNT_CONFIGURATION_ERROR, "Служебная запись состояния скидки отсутствует");
    }
}
