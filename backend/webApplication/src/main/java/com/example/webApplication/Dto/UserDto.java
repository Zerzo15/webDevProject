package com.example.webApplication.Dto;

import java.time.Instant;

import com.example.webApplication.Entity.AccountStatus;
import com.example.webApplication.Entity.UserRoleStatus;

public record UserDto(
    Long id,
    String email,
    UserRoleStatus role,
    AccountStatus accountStatus,
    Instant createdAt
) {
}
