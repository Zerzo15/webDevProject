package com.example.webApplication.Dto;

import java.time.Instant;

public record OtpCodeDto(
    Long id,
    String code,
    Instant expiredTime
) {}
