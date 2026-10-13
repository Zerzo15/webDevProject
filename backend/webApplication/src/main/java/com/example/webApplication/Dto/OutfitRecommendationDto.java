package com.example.webApplication.Dto;

public record OutfitRecommendationDto(
    Long id,
    Long userId,
    String contextPrompt,
    String aiResponseText,
    String recommendedVariantIds
) {
}
