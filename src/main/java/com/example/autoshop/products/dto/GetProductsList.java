package com.example.autoshop.products.dto;

import com.example.autoshop.products.model.Brand;
import com.example.autoshop.products.model.Category;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
* DTO for {@link com.example.autoshop.products.model.Product}
*/
public record GetProductsList(
        Long id,
        String name,
        BigDecimal sellingPrice,
        OffsetDateTime lastSupplyDate,
        Category category,
        String description,
        String sku,
        Brand brand,
        String oemNumber) {}
