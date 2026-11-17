package com.example.techstore.service;

import com.example.techstore.dto.PageResponse;
import com.example.techstore.dto.ProductForms;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.dto.ProductResponse;
import com.example.techstore.model.product.ProductCategory;

import java.math.BigDecimal;

/** Операции каталога. Контроллер зависит от этого контракта, а не от реализации. */
public interface ProductService {
    ProductResponse get(int article);

    PageResponse<ProductResponse> list(
            String name,
            String brand,
            ProductCategory category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean available,
            int page,
            int size);

    ProductResponse create(ProductRequest request);

    ProductResponse createFromForm(ProductRequest request);

    ProductResponse updateDetails(int article, ProductForms.Details request);

    ProductResponse update(int article, ProductRequest request);

    void delete(int article);
}
