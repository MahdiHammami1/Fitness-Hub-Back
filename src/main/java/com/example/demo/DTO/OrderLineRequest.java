package com.example.demo.DTO;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderLineRequest {
    @NotBlank
    private String productId;

    private String variantId; // optional

    @NotNull
    @Min(1)
    private Integer qty;
}
