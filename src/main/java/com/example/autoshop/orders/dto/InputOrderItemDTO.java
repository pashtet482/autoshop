package com.example.autoshop.orders.dto;

public record InputOrderItemDTO(
        Long productId,
        Integer quantity
) {
}
