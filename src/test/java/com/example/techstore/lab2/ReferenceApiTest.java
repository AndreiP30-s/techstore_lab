package com.example.techstore.lab2;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.Characteristic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@WithMockUser(roles = "ADMIN")
class ReferenceApiTest extends DatabaseFreeTest {
    @Autowired MockMvc mvc;

    @Test
    void createsAndRejectsDuplicateBrands() throws Exception {
        mvc.perform(
                        post("/api/brands")
                                .contentType("application/json")
                                .content("{\"name\":\" Brand \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Brand"));
        when(brands.existsById("Brand")).thenReturn(true);
        mvc.perform(
                        post("/api/brands")
                                .contentType("application/json")
                                .content("{\"name\":\"Brand\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void preventsDeletingReferencedBrand() throws Exception {
        when(brands.existsById("Brand")).thenReturn(true);
        when(products.existsByBrandName("Brand")).thenReturn(true);
        mvc.perform(delete("/api/brands/Brand")).andExpect(status().isConflict());
        verify(brands, never()).deleteById(any());
    }

    @Test
    void listsSpecificationsAndCategorySpecifications() throws Exception {
        Characteristic specification = new Characteristic();
        specification.setName("color");
        specification.setValueType("TEXT");
        when(categories.existsById("SMARTPHONE")).thenReturn(true);
        when(links.findByIdCategoryCode("SMARTPHONE"))
                .thenReturn(List.of(new CategoryCharacteristic("SMARTPHONE", "ramGb")));
        mvc.perform(get("/api/categories/SMARTPHONE/specifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ramGb"));
        when(specifications.findAll()).thenReturn(List.of(specification));
        mvc.perform(get("/api/specifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("color"));
    }

    @Test
    void readsBrandByNameAndReturns404ForMissingBrand() throws Exception {
        when(brands.findById("DemoBrand")).thenReturn(Optional.of(new Brand("DemoBrand")));
        mvc.perform(get("/api/brands/DemoBrand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("DemoBrand"));
        mvc.perform(get("/api/brands/Missing")).andExpect(status().isNotFound());
    }
}
