package com.example.autoshop.users.dto;

import com.example.autoshop.users.model.UserRole;

public record InputUserDTO(
        String username,
        String password,
        String email,
        String deliveryAddress,
        String companyName,
        String phone,
        UserRole role,
        Long priceLevelId
) {
}
