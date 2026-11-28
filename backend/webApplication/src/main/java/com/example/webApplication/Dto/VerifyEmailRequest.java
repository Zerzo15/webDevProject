package com.example.webApplication.Dto;

public record VerifyEmailRequest(
    String email,
    String code
) {}
