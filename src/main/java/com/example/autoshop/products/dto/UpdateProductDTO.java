package com.example.autoshop.products.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductDTO(
        String name,
        BigDecimal sellingPrice,
        Long categoryId,
        String description,
        String sku,
        Long brandId,
        String oemNumber,
        List<ProductAttributeDTO> attributes,
        String imageUrl
) {
}
