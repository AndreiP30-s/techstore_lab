package com.example.techstore.model.product;

import com.example.techstore.model.Validation;

public record FrontCamera(double megapixels) {
    public FrontCamera { Validation.positive(megapixels, "megapixels"); }
    public String getSpecifications() { return megapixels + " Мп"; }
}
