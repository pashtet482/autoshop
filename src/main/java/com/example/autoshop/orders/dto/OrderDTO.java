package com.example.autoshop.orders.dto;

import com.example.autoshop.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long userId,
        OrderStatus orderStatus,
        OffsetDateTime updatedAt,
        OffsetDateTime dateOfPurchase,
        OffsetDateTime dateOfDelivery,
        String deliveryAddress,
        BigDecimal subtotal,
        String discountType,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal taxPercent,
        BigDecimal taxAmount,
        BigDecimal totalPrice,
        List<OrderItemDTO> items
) {
}
