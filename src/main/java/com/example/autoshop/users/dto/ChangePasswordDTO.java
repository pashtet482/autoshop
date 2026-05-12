package com.example.autoshop.users.dto;

public record ChangePasswordDTO(
        String oldPassword,
        String newPassword
) {
}
