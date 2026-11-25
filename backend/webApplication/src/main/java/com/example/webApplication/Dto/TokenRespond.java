package com.example.webApplication.Dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TokenRespond(
    String accessToken,
    String refreshToken
) {
    //ignore refreshToken
    //meaning: the frontend would now receive 
    //the refresh token inside the json payload
    //it will be store directly inside of the website cookies
    @JsonIgnore
    @Override
    public String refreshToken() {
        return refreshToken;
    }
}
