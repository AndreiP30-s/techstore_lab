package com.example.techstore.service.impl;

import com.example.techstore.dto.ReferenceDtos.BrandDto;
import com.example.techstore.dto.ReferenceDtos.CategoryDto;
import com.example.techstore.dto.ReferenceDtos.CharacteristicDto;
import com.example.techstore.exception.ConflictException;
import com.example.techstore.exception.NotFoundException;
import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.CategoryCharacteristicId;
import com.example.techstore.model.catalog.Characteristic;
import com.example.techstore.repository.BrandRepository;
import com.example.techstore.repository.CategoryCharacteristicRepository;
import com.example.techstore.repository.CategoryRepository;
import com.example.techstore.repository.CharacteristicRepository;
import com.example.techstore.repository.ProductRepository;
import com.example.techstore.service.ProductRules;
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
                .orElseThrow(() -> new NotFoundException("Бренд не найден: " + name));
    }

    @Transactional
    @Override
    public BrandDto createBrand(BrandDto request) {
        String name = request.name().strip();
        if (brands.existsById(name)) throw new ConflictException("Бренд уже существует");
        brands.saveAndFlush(new com.example.techstore.model.catalog.Brand(name));
        return new BrandDto(name);
    }

    @Transactional
    @Override
    public BrandDto renameBrand(String old, BrandDto request) {
        if (!brands.existsById(old)) throw new NotFoundException("Бренд не найден");
        String name = request.name().strip();
        if (old.equals(name)) return new BrandDto(name);
        if (products.existsByBrandName(old))
            throw new ConflictException("Нельзя переименовать используемый бренд");
        BrandDto result = createBrand(request);
        brands.deleteById(old);
        brands.flush();
        return result;
    }

    @Transactional
    @Override
    public void deleteBrand(String name) {
        if (!brands.existsById(name)) throw new NotFoundException("Бренд не найден");
        if (products.existsByBrandName(name))
            throw new ConflictException("Бренд используется товарами");
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
    public CharacteristicDto specification(String name) {
        return dto(requireSpecification(name));
    }

    @Transactional
    @Override
    public CharacteristicDto createSpecification(CharacteristicDto request) {
        String name = request.name().strip();
        if (specifications.existsById(name))
            throw new ConflictException("Характеристика уже существует");
        Characteristic entity = new Characteristic();
        entity.setName(name);
        assign(entity, request);
        specifications.saveAndFlush(entity);
        return dto(entity);
    }

    @Transactional
    @Override
    public CharacteristicDto updateSpecification(String name, CharacteristicDto request) {
        Characteristic entity = requireSpecification(name);
        if (!name.equals(request.name().strip()))
            throw new IllegalArgumentException("Имя характеристики — неизменяемый ключ");
        if (!entity.getValueType().equals(request.valueType())
                && (products.existsBySpecificationsIdSpecificationName(name)
                        || links.existsByIdSpecificationName(name)))
            throw new ConflictException("Тип используемой характеристики менять нельзя");
        assign(entity, request);
        specifications.flush();
        return dto(entity);
    }

    @Transactional
    @Override
    public void deleteSpecification(String name) {
        Characteristic entity = requireSpecification(name);
        if (links.existsByIdSpecificationName(name)
                || products.existsBySpecificationsIdSpecificationName(name))
            throw new ConflictException("Характеристика используется");
        specifications.delete(entity);
        specifications.flush();
    }

    @Override
    public List<String> categorySpecifications(String code) {
        requireCategory(code);
        return links.findByIdCategoryCode(code).stream()
                .map(e -> e.getId().getSpecificationName())
                .sorted()
                .toList();
    }

    @Transactional
    @Override
    public void link(String code, String name) {
        requireCategory(code);
        requireSpecification(name);
        CategoryCharacteristicId id = new CategoryCharacteristicId(code, name);
        if (!links.existsById(id)) links.saveAndFlush(new CategoryCharacteristic(code, name));
    }

    @Transactional
    @Override
    public void unlink(String code, String name) {
        requireCategory(code);
        CategoryCharacteristicId id = new CategoryCharacteristicId(code, name);
        if (!links.existsById(id)) throw new NotFoundException("Связь не найдена");
        if (ProductRules.required(com.example.techstore.model.product.ProductCategory.valueOf(code))
                .contains(name))
            throw new ConflictException("Характеристика обязательна для модели товара");
        if (products.existsBySpecificationsIdSpecificationName(name))
            throw new ConflictException("Характеристика используется товарами");
        links.deleteById(id);
        links.flush();
    }

    private void requireCategory(String code) {
        if (!categories.existsById(code)) throw new NotFoundException("Категория не найдена");
    }

    private Characteristic requireSpecification(String name) {
        return specifications
                .findById(name)
                .orElseThrow(() -> new NotFoundException("Характеристика не найдена"));
    }

    private CharacteristicDto dto(Characteristic entity) {
        return new CharacteristicDto(entity.getName(), entity.getValueType(), entity.getUnit());
    }

    private void assign(Characteristic entity, CharacteristicDto request) {
        if (request.unit() != null && request.unit().isBlank())
            throw new IllegalArgumentException("Единица не должна быть пустой");
        entity.setValueType(request.valueType());
        entity.setUnit(request.unit() == null ? null : request.unit().strip());
    }
}
