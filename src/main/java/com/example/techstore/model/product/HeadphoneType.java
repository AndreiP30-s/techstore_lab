package com.example.techstore.model.product;

public enum HeadphoneType {
    IN_EAR("внутриканальные"),
    ON_EAR("накладные"),
    OVER_EAR("полноразмерные");

    private final String displayName;

    HeadphoneType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
