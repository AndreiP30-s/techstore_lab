package com.example.techstore.repository;

import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

public final class ProductFilters {
    private ProductFilters() {}

    public static Specification<Product> matching(
            String name,
            String brand,
            ProductCategory category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean available) {
        return (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates =
                    new java.util.ArrayList<>();
            if (name != null && !name.isBlank()) {
                String escaped =
                        name.strip()
                                .toLowerCase(Locale.ROOT)
                                .replace("\\", "\\\\")
                                .replace("%", "\\%")
                                .replace("_", "\\_");
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '\\'));
            }
            if (brand != null)
                predicates.add(cb.equal(root.get("brand").get("name"), brand.strip()));
            if (category != null)
                predicates.add(cb.equal(root.get("category").get("code"), category.name()));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (available != null) predicates.add(cb.equal(root.get("available"), available));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
