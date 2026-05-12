package com.example.autoshop.orders.dto;

import java.util.List;

public record InputOrderDTO(
        Long userId,
        String deliveryAddress,
        List<InputOrderItemDTO> items
) {
}
