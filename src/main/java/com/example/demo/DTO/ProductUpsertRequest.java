package com.example.demo.DTO;

import com.example.demo.model.ProductCollection;
import com.example.demo.model.ProductImage;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpsertRequest {
    @NotBlank
    private String title;
    private String description;

    @NotNull
    private BigDecimal price;
    @NotNull
    private ProductCollection collection;

    private List<ProductImage> images;

    @NotNull
    private Boolean hasVariants;
    private Integer stock; // required if hasVariants=false

    @NotNull
    private Boolean isActive;
}
