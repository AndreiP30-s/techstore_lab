package com.example.techstore.lab2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.techstore.exception.ValidationException;
import com.example.techstore.exception.ConflictException;
import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.catalog.Category;
import com.example.techstore.model.discount.DiscountBackup;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductSpecification;
import com.example.techstore.service.DiscountService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class DiscountApiTest extends DatabaseFreeTest {
    @Autowired DiscountService service;

    @Test
    void restoresPricesFromDatabaseBackupAfterRestart() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(true);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        Product product = new Product();
        product.setArticle(1);
        product.setPrice(new BigDecimal("90.00"));
        when(products.findById(1)).thenReturn(Optional.of(product));
        when(backups.findAll())
                .thenReturn(List.of(new DiscountBackup(1, new BigDecimal("100.00"))));
        assertEquals(1, service.cancel().affected());
        assertEquals(new BigDecimal("100.00"), product.getPrice());
        assertFalse(state.isActive());
        verify(backups).deleteAllInBatch();
    }

    @Test
    void rejectsSecondDiscount() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(true);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        assertThrows(ConflictException.class, () -> service.apply(BigDecimal.TEN, 2));
        verify(products, never()).findAll();
    }

    @Test
    void savesOldPricesAndChangesPricesAfterParallelCalculation() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(false);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        Product product = headphones();
        when(products.findAll()).thenReturn(List.of(product));
        assertEquals(1, service.apply(BigDecimal.TEN, 2).affected());
        assertEquals(new BigDecimal("90.00"), product.getPrice());
        assertTrue(state.isActive());
        org.mockito.ArgumentCaptor<DiscountBackup> saved =
                org.mockito.ArgumentCaptor.forClass(DiscountBackup.class);
        verify(backups).save(saved.capture());
        assertEquals(new BigDecimal("100.00"), saved.getValue().getOriginalPrice());
        assertEquals(1, saved.getValue().getProductArticle());
    }

    @Test
    void rejectsDiscountThatWouldRoundPriceToZeroBeforeChangingAnything() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(false);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        Product product = headphones();
        product.setPrice(new BigDecimal("0.01"));
        when(products.findAll()).thenReturn(List.of(product));
        assertThrows(
                ValidationException.class, () -> service.apply(new BigDecimal("99.99"), 2));
        assertFalse(state.isActive());
        assertEquals(new BigDecimal("0.01"), product.getPrice());
        verify(backups, never()).save(any());
    }

    private Product headphones() {
        Product product = new Product();
        product.setArticle(1);
        product.setName("Audio");
        product.setPrice(new BigDecimal("100.00"));
        product.setBrand(new Brand("Brand"));
        Category category = new Category();
        category.setCode("HEADPHONES");
        product.setCategory(category);
        java.util.Map<String, String> values =
                java.util.Map.of(
                        "headphoneType",
                        "OVER_EAR",
                        "wireless",
                        "true",
                        "noiseCancelling",
                        "false",
                        "minFrequencyHz",
                        "20",
                        "maxFrequencyHz",
                        "20000");
        values.forEach(
                (key, value) ->
                        product.getSpecifications()
                                .add(new ProductSpecification(product, key, value)));
        return product;
    }
}
