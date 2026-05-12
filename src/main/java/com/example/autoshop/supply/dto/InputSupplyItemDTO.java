package com.example.autoshop.supply.dto;

import java.math.BigDecimal;

public record InputSupplyItemDTO(
        Long productId,
        Long warehouseId,
        Integer quantity,
        BigDecimal purchasePrice
) {
}
