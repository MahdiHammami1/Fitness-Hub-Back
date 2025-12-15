package com.example.demo.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    private String url;
    private String altText; // for accessibility
    private String title;   // optional title
    private Integer position; // ordering index (null means append order)
}

