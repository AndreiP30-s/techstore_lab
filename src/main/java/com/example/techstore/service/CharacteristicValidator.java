package com.example.techstore.service;

import com.example.techstore.dto.ProductRequest;
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
            throw new IllegalArgumentException("Характеристики обязательны");
        for (Map.Entry<String, String> entry : request.specifications().entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank())
                throw new IllegalArgumentException(
                        "Пустое значение характеристики: " + entry.getKey());
        }
        rules.validate(request);
        Set<String> allowed = new HashSet<>();
        for (CategoryCharacteristic link : links.findByIdCategoryCode(request.category().name()))
            allowed.add(link.getId().getSpecificationName());
        for (Map.Entry<String, String> entry : request.specifications().entrySet()) {
            if (!allowed.contains(entry.getKey()))
                throw new IllegalArgumentException(
                        "Характеристика не разрешена для категории: " + entry.getKey());
            Characteristic specification =
                    specifications
                            .findById(entry.getKey())
                            .orElseThrow(() -> new NotFoundException("Характеристика не найдена"));
            String value = entry.getValue().strip();
            try {
                switch (specification.getValueType()) {
                    case "INTEGER" -> Integer.parseInt(value);
                    case "DECIMAL" -> new BigDecimal(value);
                    case "BOOLEAN" -> {
                        if (!value.equals("true") && !value.equals("false"))
                            throw new IllegalArgumentException();
                    }
                    case "TEXT", "ENUM" -> {
                        if (value.isBlank()) throw new IllegalArgumentException();
                    }
                    default -> throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Неверный тип значения: " + entry.getKey());
            }
        }
    }
}
