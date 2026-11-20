package com.example.webApplication.Dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(List<CartItemDto> items, BigDecimal subtotal, boolean hasStockProblem) {
}
