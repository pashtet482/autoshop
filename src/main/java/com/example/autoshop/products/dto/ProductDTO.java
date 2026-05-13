package com.example.autoshop.products.dto;

import java.math.BigDecimal;
import java.util.List;

/**
* DTO for {@link com.example.autoshop.products.model.Product}
*/
public record ProductDTO(
        Long id,
        String name,
        BigDecimal sellingPrice,
        CategoryDTO category,
        String description,
        String sku,
        BrandDTO brand,
        String oemNumber,
        List<ProductAttributeDTO> attributes,
        List<ProductStockDTO> stocks) {}
