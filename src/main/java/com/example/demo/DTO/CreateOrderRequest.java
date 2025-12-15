package com.example.demo.DTO;

import com.example.demo.model.PaymentProvider;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String customerName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @Valid
    @NotNull
    private AddressDto address;

    @NotNull
    private PaymentProvider provider; // TEST or COD

    @NotEmpty
    private List<OrderLineRequest> items;
}
