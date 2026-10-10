package com.example.webApplication.Dto;

import java.time.Instant;

public record StoreDto(
    Long id,
    UserDto appUser,
    String name,
    String description,
    String contact_email,
    boolean isActive,
    Instant create_at,
    Instant updated_at
) {

}
