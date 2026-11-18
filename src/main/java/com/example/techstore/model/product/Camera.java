package com.example.techstore.model.product;

import com.example.techstore.model.Validation;
import java.util.Objects;

public record Camera(CameraType type, double megapixels) {
    public Camera {
        Objects.requireNonNull(type, "type");
        Validation.positive(megapixels, "megapixels");
    }

    public String getSpecifications() {
        return type.getDisplayName() + " " + megapixels + " Мп";
    }
}
