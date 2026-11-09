package com.example.techstore.model.product;

public enum CameraType {
    WIDE("широкоугольная"),
    ULTRA_WIDE("сверхширокоугольная"),
    TELEPHOTO("телефото"),
    MACRO("макро");

    private final String displayName;
    CameraType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
