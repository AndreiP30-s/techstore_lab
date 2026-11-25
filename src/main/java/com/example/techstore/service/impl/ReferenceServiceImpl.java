package com.example.techstore.service.impl;

import com.example.techstore.dto.ReferenceDtos.BrandDto;
import com.example.techstore.dto.ReferenceDtos.CategoryDto;
import com.example.techstore.dto.ReferenceDtos.CharacteristicDto;
import com.example.techstore.exception.ConflictException;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.NotFoundException;
import com.example.techstore.model.catalog.Characteristic;
import com.example.techstore.repository.BrandRepository;
import com.example.techstore.repository.CategoryCharacteristicRepository;
import com.example.techstore.repository.CategoryRepository;
import com.example.techstore.repository.CharacteristicRepository;
import com.example.techstore.repository.ProductRepository;
import com.example.techstore.service.ReferenceService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReferenceServiceImpl implements ReferenceService {
    private final BrandRepository brands;
    private final CategoryRepository categories;
    private final CharacteristicRepository specifications;
    private final CategoryCharacteristicRepository links;
    private final ProductRepository products;

    public ReferenceServiceImpl(
            BrandRepository brands,
            CategoryRepository categories,
            CharacteristicRepository specifications,
            CategoryCharacteristicRepository links,
            ProductRepository products) {
        this.brands = brands;
        this.categories = categories;
        this.specifications = specifications;
        this.links = links;
        this.products = products;
    }

    @Override
    public List<BrandDto> brands() {
        return brands.findAll().stream().map(b -> new BrandDto(b.getName())).toList();
    }

    @Override
    public BrandDto brand(String name) {
        return brands.findById(name)
                .map(brand -> new BrandDto(brand.getName()))
                .orElseThrow(() -> new NotFoundException(ErrorCode.BRAND_NOT_FOUND, "Бренд не найден: " + name));
    }

    @Transactional
    @Override
    public BrandDto createBrand(BrandDto request) {
        String name = request.name().strip();
        if (brands.existsById(name))
            throw new ConflictException(ErrorCode.DUPLICATE_BRAND, "Бренд уже существует");
        brands.saveAndFlush(new com.example.techstore.model.catalog.Brand(name));
        return new BrandDto(name);
    }

    @Transactional
    @Override
    public BrandDto renameBrand(String old, BrandDto request) {
        if (!brands.existsById(old))
            throw new NotFoundException(ErrorCode.BRAND_NOT_FOUND, "Бренд не найден");
        String name = request.name().strip();
        if (old.equals(name)) return new BrandDto(name);
        if (products.existsByBrandName(old))
            throw new ConflictException(
                    ErrorCode.BRAND_IN_USE,
                    "Нельзя переименовать используемый бренд");
        BrandDto result = createBrand(request);
        brands.deleteById(old);
        brands.flush();
        return result;
    }

    @Transactional
    @Override
    public void deleteBrand(String name) {
        if (!brands.existsById(name))
            throw new NotFoundException(ErrorCode.BRAND_NOT_FOUND, "Бренд не найден");
        if (products.existsByBrandName(name))
            throw new ConflictException(ErrorCode.BRAND_IN_USE, "Бренд используется товарами");
        brands.deleteById(name);
        brands.flush();
    }

    @Override
    public List<CategoryDto> categories() {
        return categories.findAll().stream()
                .map(c -> new CategoryDto(c.getCode(), c.getName()))
                .toList();
    }

    @Override
    public List<CharacteristicDto> specifications() {
        return specifications.findAll().stream().map(this::dto).toList();
    }

    @Override
    public List<String> categorySpecifications(String code) {
        requireCategory(code);
        return links.findByIdCategoryCode(code).stream()
                .map(e -> e.getId().getSpecificationName())
                .sorted()
                .toList();
    }

    private void requireCategory(String code) {
        if (!categories.existsById(code))
            throw new NotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Категория не найдена");
    }

    private CharacteristicDto dto(Characteristic entity) {
        return new CharacteristicDto(entity.getName(), entity.getValueType(), entity.getUnit());
    }
}
