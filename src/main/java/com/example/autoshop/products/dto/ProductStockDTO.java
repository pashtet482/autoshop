package com.example.autoshop.products.dto;

public record ProductStockDTO(
        Long warehouseId,
        String warehouseAddress,
        Integer warehouseDeliveryDays,
        Integer quantity
) {
}
