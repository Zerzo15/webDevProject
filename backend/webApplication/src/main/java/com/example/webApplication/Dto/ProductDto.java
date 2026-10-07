package com.example.webApplication.Dto;

public record ProductDto(
    String name,
    String catagory,
    Long price,
    Long stock_quantity,
    Boolean is_selling
) {

}
