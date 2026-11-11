package com.example.webApplication.Dto;

public record BodyProfileDto(
    Long id,
    Long userId,
    String gender,
    int heightCm,
    int weightKg,
    int bustCm,
    int waistCm,
    int hipsCm
) {
}
