package com.example.autoshop.supply.dto;

public record WarehouseDTO(
        Long id,
        String address,
        String phone,
        Integer deliveryDays) {
}
