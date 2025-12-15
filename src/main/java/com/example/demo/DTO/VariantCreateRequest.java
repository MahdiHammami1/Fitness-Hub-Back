package com.example.demo.DTO;

import com.example.demo.model.VariantType;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class VariantCreateRequest {
    @NotNull
    private VariantType variantType;

    @NotBlank
    private String value;

    @NotNull
    @Min(0)
    private Integer stock;
}
