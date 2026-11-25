package com.example.techstore.service;

import com.example.techstore.dto.ProductRequest;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.InvalidCharacteristicException;
import com.example.techstore.exception.NotFoundException;
import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.Characteristic;
import com.example.techstore.repository.CategoryCharacteristicRepository;
import com.example.techstore.repository.CharacteristicRepository;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Проверяет допустимость характеристик для категории и типы значений по справочнику.
 */
@Component
public class CharacteristicValidator {
    private final CategoryCharacteristicRepository links;
    private final CharacteristicRepository specifications;
    private final ProductRules rules;

    public CharacteristicValidator(
            CategoryCharacteristicRepository links,
            CharacteristicRepository specifications,
            ProductRules rules) {
        this.links = links;
        this.specifications = specifications;
        this.rules = rules;
    }

    public void validate(ProductRequest request) {
        if (request.specifications() == null)
            throw new InvalidCharacteristicException(
                    ErrorCode.MISSING_CHARACTERISTIC,
                    "Характеристики обязательны");
        for (Map.Entry<String, String> entry : request.specifications().entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank())
                throw new InvalidCharacteristicException(
                        ErrorCode.INVALID_CHARACTERISTIC_VALUE,
                        "Пустое значение характеристики: " + entry.getKey(),
                        "Передайте непустое значение для " + entry.getKey() + ".");
        }
        rules.validate(request);
        Set<String> allowed = new HashSet<>();
        for (CategoryCharacteristic link : links.findByIdCategoryCode(request.category().name()))
            allowed.add(link.getId().getSpecificationName());
        for (Map.Entry<String, String> entry : request.specifications().entrySet()) {
            if (!allowed.contains(entry.getKey()))
                throw new InvalidCharacteristicException(
                        ErrorCode.CHARACTERISTIC_NOT_ALLOWED,
                        "Характеристика не разрешена для категории: " + entry.getKey());
            Characteristic specification =
                    specifications
                            .findById(entry.getKey())
                            .orElseThrow(() -> new NotFoundException(ErrorCode.CHARACTERISTIC_NOT_FOUND, "Характеристика не найдена: " + entry.getKey()));
            String value = entry.getValue().strip();
            String valueType = specification.getValueType();
            if (!isValidValue(valueType, value)) {
                String hint = switch (valueType) {
                    case "INTEGER" -> "Укажите целое число, например 8.";
                    case "DECIMAL" -> "Укажите число, например 6.5.";
                    case "BOOLEAN" -> "Укажите true или false.";
                    default -> ErrorCode.INVALID_CHARACTERISTIC_VALUE.getHint();
                };
                throw new InvalidCharacteristicException(
                        ErrorCode.INVALID_CHARACTERISTIC_VALUE,
                        "Неверный тип значения характеристики " + entry.getKey() + ": ожидается " + valueType,
                        hint);
            }
        }
    }

    private boolean isValidValue(String valueType, String value) {
        try {
            return switch (valueType) {
                case "INTEGER" -> {
                    Integer.parseInt(value);
                    yield true;
                }
                case "DECIMAL" -> {
                    new BigDecimal(value);
                    yield true;
                }
                case "BOOLEAN" -> value.equals("true") || value.equals("false");
                case "TEXT", "ENUM" -> !value.isBlank();
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
