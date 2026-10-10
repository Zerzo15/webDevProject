package com.example.webApplication.Dto;

import java.time.Instant;

public record CartDto(
    Long id,
    UserDto appUser,
    Instant create_at,
    Instant updated_at
) {

}
