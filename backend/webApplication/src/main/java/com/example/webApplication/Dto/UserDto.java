package com.example.webApplication.Dto;

import com.example.webApplication.Entity.UserRoleStatus;

public record UserDto(
    Long id,
    String last_name,
    String first_name,
    UserRoleStatus role
) {

}
