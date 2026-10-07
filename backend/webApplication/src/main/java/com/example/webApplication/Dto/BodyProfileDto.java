package com.example.webApplication.Dto;

public record BodyProfileDto(
    UserDto user,
    String gender,
    int height_cm,
    int weight_kg,
    int bust_cm,
    int hips_cm
) {

}
