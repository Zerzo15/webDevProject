package com.example.webApplication.Dto;

public record TokenRespond(
    String accessToken,
    String refreshToken
) {}
