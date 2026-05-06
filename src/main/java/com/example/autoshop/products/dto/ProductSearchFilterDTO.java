package com.example.autoshop.products.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for {@link com.example.autoshop.products.model.Product}
 */
public record ProductSearchFilterDTO(
        String name,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long categoryId,
        Long brandId,
        String sku,
        String oemNumber,
        Boolean inStock,
        Map<String, List<String>> attributes
) {}
