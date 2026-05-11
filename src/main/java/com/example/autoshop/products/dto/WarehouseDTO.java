package com.example.autoshop.products.dto;

public record WarehouseDTO(
        Long id,
        String address,
        String phone,
        Integer deliveryDays) {
}
