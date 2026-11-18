package com.example.techstore.lab2;

import com.example.techstore.repository.BrandRepository;
import com.example.techstore.repository.CategoryCharacteristicRepository;
import com.example.techstore.repository.CategoryRepository;
import com.example.techstore.repository.CharacteristicRepository;
import com.example.techstore.repository.DiscountBackupRepository;
import com.example.techstore.repository.DiscountStateRepository;
import com.example.techstore.repository.ProductRepository;
import com.example.techstore.repository.UserRepository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class DatabaseFreeTest {
    @MockitoBean protected ProductRepository products;
    @MockitoBean protected BrandRepository brands;
    @MockitoBean protected CategoryRepository categories;
    @MockitoBean protected CharacteristicRepository specifications;
    @MockitoBean protected CategoryCharacteristicRepository links;
    @MockitoBean protected UserRepository users;
    @MockitoBean protected DiscountStateRepository discounts;
    @MockitoBean protected DiscountBackupRepository backups;
}
