package com.example.demo.DTO;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class AddressDto {
    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private String zip;

    @NotBlank
    private String line1;

    private String line2;
}
