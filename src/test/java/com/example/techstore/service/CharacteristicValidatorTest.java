package com.example.techstore.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.techstore.exception.InvalidCharacteristicException;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.repository.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

class CharacteristicValidatorTest {
    @Test
    void rejectsNullOptionalValueEvenWithoutMvcValidation() {
        CategoryCharacteristicRepository links = mock(CategoryCharacteristicRepository.class);
        CharacteristicRepository definitions = mock(CharacteristicRepository.class);
        CharacteristicValidator validator =
                new CharacteristicValidator(links, definitions, new ProductRules());
        Map<String, String> values = new HashMap<>();
        values.put("color", null);
        ProductRequest request =
                new ProductRequest(
                        "Audio",
                        "Brand",
                        ProductCategory.HEADPHONES,
                        BigDecimal.TEN,
                        true,
                        values,
                        List.of());
        InvalidCharacteristicException error =
                assertThrows(InvalidCharacteristicException.class, () -> validator.validate(request));
        assertTrue(error.getMessage().contains("color"));
        verifyNoInteractions(links, definitions);
    }
}
