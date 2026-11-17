package com.example.techstore.lab2;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.Characteristic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

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
    void managesSpecificationAndCategoryLink() throws Exception {
        Characteristic specification = new Characteristic();
        specification.setName("color");
        specification.setValueType("TEXT");
        when(specifications.findById("color")).thenReturn(Optional.of(specification));
        when(categories.existsById("SMARTPHONE")).thenReturn(true);
        mvc.perform(
                        post("/api/specifications")
                                .contentType("application/json")
                                .content(
                                        "{\"name\":\"color\",\"valueType\":\"TEXT\",\"unit\":null}"))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/categories/SMARTPHONE/specifications/color"))
                .andExpect(status().isNoContent());
        verify(links).saveAndFlush(any(CategoryCharacteristic.class));
        when(links.findByIdCategoryCode("SMARTPHONE"))
                .thenReturn(List.of(new CategoryCharacteristic("SMARTPHONE", "color")));
        mvc.perform(get("/api/categories/SMARTPHONE/specifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("color"));
        when(links.existsByIdSpecificationName("color")).thenReturn(true);
        mvc.perform(delete("/api/specifications/color")).andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidSpecificationTypeAndChangingUsedType() throws Exception {
        mvc.perform(
                        post("/api/specifications")
                                .contentType("application/json")
                                .content("{\"name\":\"color\",\"valueType\":\"WRONG\"}"))
                .andExpect(status().isBadRequest());
        Characteristic specification = new Characteristic();
        specification.setName("ramGb");
        specification.setValueType("INTEGER");
        when(specifications.findById("ramGb")).thenReturn(Optional.of(specification));
        when(links.existsByIdSpecificationName("ramGb")).thenReturn(true);
        mvc.perform(
                        put("/api/specifications/ramGb")
                                .contentType("application/json")
                                .content("{\"name\":\"ramGb\",\"valueType\":\"TEXT\"}"))
                .andExpect(status().isConflict());
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
