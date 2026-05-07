package com.example.autoshop.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for {@link com.example.autoshop.products.model.Brand}
 */
public record BrandDTO(
        Long id,

        @Size(max = 128)
        @NotBlank
        String name,

        @Size(max = 64)
        @NotBlank
        String country) {
}