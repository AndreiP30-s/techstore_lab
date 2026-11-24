package com.example.techstore.service.impl;

import com.example.techstore.dto.PageResponse;
import com.example.techstore.dto.ProductForms;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.dto.ProductResponse;
import com.example.techstore.exception.DiscountConfigurationException;
import com.example.techstore.exception.DiscountStateException;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.NotFoundException;
import com.example.techstore.exception.ValidationException;
import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.ProductSpecification;
import com.example.techstore.model.product.RearCamera;
import com.example.techstore.repository.BrandRepository;
import com.example.techstore.repository.CategoryRepository;
import com.example.techstore.repository.DiscountStateRepository;
import com.example.techstore.repository.ProductFilters;
import com.example.techstore.repository.ProductRepository;
import com.example.techstore.service.CharacteristicValidator;
import com.example.techstore.service.ProductService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository products;
    private final BrandRepository brands;
    private final CategoryRepository categories;
    private final CharacteristicValidator validator;
    private final DiscountStateRepository discounts;

    public ProductServiceImpl(
            ProductRepository products,
            BrandRepository brands,
            CategoryRepository categories,
            CharacteristicValidator validator,
            DiscountStateRepository discounts) {
        this.products = products;
        this.brands = brands;
        this.categories = categories;
        this.validator = validator;
        this.discounts = discounts;
    }

    @Override
    public ProductResponse get(int article) {
        return ProductResponse.from(require(article));
    }

    @Override
    public PageResponse<ProductResponse> list(
            String name,
            String brand,
            ProductCategory category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean available,
            int page,
            int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new ValidationException(
                    ErrorCode.INVALID_PAGINATION,
                    "Недопустимые параметры пагинации: page=" + page + ", size=" + size);
        if ((minPrice != null && minPrice.signum() < 0)
                || (maxPrice != null && maxPrice.signum() < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0))
            throw new ValidationException(
                    ErrorCode.INVALID_PRICE_RANGE,
                    "Некорректный диапазон цен: minPrice=" + minPrice + ", maxPrice=" + maxPrice);
        return PageResponse.from(
                products.findAll(
                                ProductFilters.matching(
                                        name, brand, category, minPrice, maxPrice, available),
                                PageRequest.of(page, size, Sort.by("article")))
                        .map(ProductResponse::from));
    }

    @Transactional
    @Override
    public ProductResponse create(ProductRequest request) {
        lockDiscounts();
        Product product = new Product();
        validateAndSet(product, request);
        // Получаем артикул для составных ключей характеристик и камер.
        products.saveAndFlush(product);
        addChildren(product, request);
        products.flush();
        return ProductResponse.from(product);
    }

    @Transactional
    @Override
    public ProductResponse createFromForm(ProductRequest request) {
        lockDiscounts();
        String name = request.brand().strip();
        if (!brands.existsById(name)) brands.saveAndFlush(new Brand(name));
        return create(request);
    }

    @Transactional
    @Override
    public ProductResponse updateDetails(int article, ProductForms.Details request) {
        if (request.name() == null
                && request.brand() == null
                && request.price() == null
                && request.available() == null)
            throw new ValidationException(
                    ErrorCode.EMPTY_UPDATE,
                    "Заполните хотя бы одно поле для изменения");
        lockDiscounts();
        Product product = require(article);
        if (request.name() != null) product.setName(request.name());
        if (request.brand() != null)
            product.setBrand(
                    brands.findById(request.brand())
                            .orElseThrow(() -> new NotFoundException(ErrorCode.BRAND_NOT_FOUND, "Бренд не найден")));
        if (request.price() != null) product.setPrice(request.price());
        if (request.available() != null) product.setAvailable(request.available());
        products.flush();
        return ProductResponse.from(product);
    }

    @Transactional
    @Override
    public ProductResponse update(int article, ProductRequest request) {
        lockDiscounts();
        Product product = require(article);
        validateAndSet(product, request);
        product.getSpecifications().clear();
        product.getRearCameras().clear();
        // Удаляем старые записи перед вставкой новых с теми же составными ключами.
        products.flush();
        addChildren(product, request);
        products.flush();
        return ProductResponse.from(product);
    }

    @Transactional
    @Override
    public void delete(int article) {
        lockDiscounts();
        products.delete(require(article));
        products.flush();
    }

    private void lockDiscounts() {
        DiscountState state =
                discounts
                        .lockState()
                        .orElseThrow(DiscountConfigurationException::new);
        if (state.isActive())
            throw new DiscountStateException(
                    ErrorCode.CATALOG_LOCKED_BY_DISCOUNT,
                    "Сначала отмените скидку, затем изменяйте каталог");
    }

    private Product require(int article) {
        return products.findById(article)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Товар не найден: " + article));
    }

    private void validateAndSet(Product product, ProductRequest request) {
        validator.validate(request);
        product.setName(request.name().strip());
        product.setPrice(request.price());
        product.setAvailable(request.available());
        product.setBrand(
                brands.findById(request.brand().strip())
                        .orElseThrow(() -> new NotFoundException(ErrorCode.BRAND_NOT_FOUND, "Бренд не найден")));
        product.setCategory(
                categories
                        .findById(request.category().name())
                        .orElseThrow(() -> new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Категория не найдена")));
    }

    private void addChildren(Product product, ProductRequest request) {
        request.specifications()
                .forEach(
                        (key, value) ->
                                product.getSpecifications()
                                        .add(
                                                new ProductSpecification(
                                                        product, key, value.strip())));
        for (ProductRequest.RearCameraRequest camera : request.rearCameras())
            product.getRearCameras()
                    .add(
                            new RearCamera(
                                    product,
                                    camera.type().name(),
                                    camera.resolutionMp(),
                                    camera.quantity()));
    }
}
