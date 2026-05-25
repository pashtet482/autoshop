package com.example.autoshop.users.dto;

import com.example.autoshop.users.model.UserRole;

import java.math.BigDecimal;

public record UserDTO(
        Long id,
        String username,
        String email,
        String deliveryAddress,
        String companyName,
        String phone,
        UserRole role,
        Long priceLevelId,
        String priceLevelName,
        BigDecimal priceLevelRatio
) {
}
