package com.example.autoshop.supply.dto;

import java.math.BigDecimal;

public record SupplyItemDTO(
        Long id,
        Long productId,
        Long warehouseId,
        Integer quantity,
        BigDecimal purchasePrice
) {
}
