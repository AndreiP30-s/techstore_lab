package com.example.techstore.dto;

import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.ProductCategory;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(
        name = "ProductRequest",
        description = "Общие поля товара, строковые характеристики и группы камер",
        example =
                """
{
  "name": "Demo Phone",
  "brand": "DemoBrand",
  "category": "SMARTPHONE",
  "price": 30000,
  "available": true,
  "specifications": {
    "processor": "Demo CPU",
    "ramGb": "8",
    "storageGb": "256",
    "screenDiagonalInches": "6.5",
    "frontCameraMp": "12",
    "batteryCapacityMah": "5000",
    "supports5g": "true"
  },
  "rearCameras": [
    {
      "type": "WIDE",
      "resolutionMp": 50,
      "quantity": 2
    },
    {
      "type": "ULTRA_WIDE",
      "resolutionMp": 12,
      "quantity": 1
    }
  ]
}
""")
public record ProductRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 100) String brand,
        @NotNull ProductCategory category,
        @NotNull @DecimalMin("0.01") @Digits(integer = 7, fraction = 2) BigDecimal price,
        @NotNull Boolean available,
        @NotNull @Size(max = 100)
                Map<@NotBlank @Size(max = 100) String, @NotBlank @Size(max = 1000) String>
                        specifications,
        @NotNull @Size(max = 32) List<@NotNull @Valid RearCameraRequest> rearCameras) {
    public record RearCameraRequest(
            @NotNull CameraType type,
            @NotNull @DecimalMin("0.001") @Digits(integer = 5, fraction = 3)
                    BigDecimal resolutionMp,
            @Min(1) @Max(16) int quantity) {}
}
